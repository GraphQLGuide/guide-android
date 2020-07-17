package guide.graphql.toc.ui.chapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.transition.MaterialSharedAxis
import guide.graphql.toc.R
import guide.graphql.toc.data.Status
import guide.graphql.toc.databinding.ChaptersFragmentBinding

class ChaptersFragment : Fragment() {
    private var _binding: ChaptersFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val viewModel: ChaptersViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChaptersFragmentBinding.inflate(inflater, container, false)
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

        requireActivity()
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

        viewModel.chapterList.observe(viewLifecycleOwner, Observer { chapterListResponse ->
            when (chapterListResponse.status) {
                Status.SUCCESS -> {
                    chapterListResponse.data?.let {
                        adapter.updateChapters(it)
                    }
                }
                Status.ERROR -> Toast.makeText(
                    requireContext(),
                    getString(R.string.graphql_error, chapterListResponse.message),
                    Toast.LENGTH_SHORT
                ).show()
                Status.LOADING -> {
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}