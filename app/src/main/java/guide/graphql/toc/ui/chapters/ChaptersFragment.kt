package guide.graphql.toc.ui.chapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.google.android.material.transition.MaterialSharedAxis
import guide.graphql.toc.ChaptersQuery
import guide.graphql.toc.R
import guide.graphql.toc.data.apolloClient
import guide.graphql.toc.databinding.ChaptersFragmentBinding

class ChaptersFragment : Fragment() {

    private lateinit var binding: ChaptersFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ChaptersFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backward = MaterialSharedAxis(MaterialSharedAxis.X, false)
        reenterTransition = backward

        val forward = MaterialSharedAxis(MaterialSharedAxis.X, true)
        exitTransition = forward
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            ChaptersAdapter(
                requireContext()
            ) { chapter ->
                findNavController().navigate(
                    ChaptersFragmentDirections.viewSections(
                        chapterId = chapter.id,
                        chapterNumber = chapter.number?.toInt() ?: -1,
                        chapterTitle = if (chapter.number == null) chapter.title else getString(
                            R.string.chapter_title,
                            chapter.number.toInt().toString(),
                            chapter.title
                        )
                    )
                )
            }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.chapters.layoutManager = layoutManager

        val itemDivider = DividerItemDecoration(requireContext(), layoutManager.orientation)
        binding.chapters.addItemDecoration(itemDivider)
        binding.chapters.adapter = adapter

        lifecycleScope.launchWhenStarted {
            // Loading
            try {
                val response = apolloClient.query(
                    ChaptersQuery()
                ).toDeferred().await()
                if (response.hasErrors()) {
                    throw Exception("Response has errors")
                }
                val chapters = response.data?.chapters ?: throw Exception("Data is null")
                // Success
                adapter.updateChapters(chapters)
            } catch (e: ApolloException) {
                // Error
                showErrorMessage("GraphQL request failed")
            } catch (e: Exception) {
                showErrorMessage(e.message.orEmpty())
            }
        }
    }


    private fun showErrorMessage(errorMessage: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.graphql_error, errorMessage),
            Toast.LENGTH_SHORT
        ).show()
    }
}