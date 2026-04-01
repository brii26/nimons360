package com.tit.nimonsapp.ui.familydetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tit.nimonsapp.R

class FamilyDetailFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val familyId = arguments?.getInt("familyId") ?: -1

        return ComposeView(requireContext()).apply {
            setContent {
                familyDetailScreen(
                    familyId = familyId,
                    onBackToFamilies = {
                        findNavController().navigate(R.id.action_familyDetailFragment_to_familiesFragment)
                    },
                )
            }
        }
    }
}

@Composable
fun familyDetailScreen(
    familyId: Int,
    onBackToFamilies: () -> Unit,
) {
    Text("Family Detail: $familyId")
}
