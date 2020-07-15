package guide.graphql.toc.ui.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.transition.MaterialSharedAxis
import guide.graphql.toc.ChaptersQuery
import guide.graphql.toc.SectionsQuery
import guide.graphql.toc.data.Status
import guide.graphql.toc.data.apolloClient
import guide.graphql.toc.databinding.SectionsFragmentBinding

class SectionsFragment : Fragment() {


    private lateinit var binding: SectionsFragmentBinding
    private val args: SectionsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SectionsFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        enterTransition = forward

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        returnTransition = backward
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            SectionsAdapter(
                requireContext(),
                args.chapterNumber
            )

        val layoutManager = LinearLayoutManager(requireContext())
        binding.sections.layoutManager = layoutManager

        val itemDivider =
            DividerItemDecoration(requireContext(), layoutManager.orientation)
        binding.sections.addItemDecoration(itemDivider)
        binding.sections.adapter = adapter

        lifecycleScope.launchWhenStarted {
            // Loading
            binding.spinner.visibility = View.VISIBLE
            binding.error.visibility = View.GONE
            try {
                val response = apolloClient.query(
                    SectionsQuery(id = args.chapterId)
                ).toDeferred().await()
                if (response.hasErrors()) {
                    throw Exception("Response has errors")
                }
                val sections = response.data?.chapter?.sections ?: throw Exception("Data is null")
                // Success
                if (sections.size > 1) {
                    adapter.updateSections(sections)
                    binding.spinner.visibility = View.GONE
                    binding.error.visibility = View.GONE
                } else {
                    throw Exception("No sections")
                }
            } catch (e: ApolloException) {
                // Error
                showErrorMessage("GraphQL request failed")
            } catch (e: Exception) {
                showErrorMessage(e.message.orEmpty())
            }
        }
    }

    private fun showErrorMessage(error: String) {
        binding.spinner.visibility = View.GONE
        binding.error.text = error
        binding.error.visibility = View.VISIBLE
    }
}