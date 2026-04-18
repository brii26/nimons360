package com.tit.nimonsapp.ui.createfamily

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.tit.nimonsapp.data.repository.FamilyRepository
import com.tit.nimonsapp.data.repository.SessionRepository
import com.tit.nimonsapp.ui.common.BaseStateViewModel
import com.tit.nimonsapp.ui.common.UiResourceMeta
import kotlinx.coroutines.launch

class CreateFamilyViewModel(
    application: Application,
) : BaseStateViewModel<CreateFamilyUiState>(application, CreateFamilyUiState()) {
    private val familyRepository = FamilyRepository()
    private val sessionRepository = SessionRepository(application)

    // ONLY ALLOWED ICONS
    companion object {
        private val ALLOWED_ICON_URLS = listOf(
            "https://mad.labpro.hmif.dev/assets/family_icon_1.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_2.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_3.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_4.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_5.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_6.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_7.png",
            "https://mad.labpro.hmif.dev/assets/family_icon_8.png",
        )
    }

    override fun CreateFamilyUiState.withMeta(meta: UiResourceMeta): CreateFamilyUiState = copy(meta = meta)

    fun onNameChanged(name: String) {
        updateState {
            copy(name = name)
        }
    }

    fun onIconUrlChanged(iconUrl: String) {
        updateState {
            copy(iconUrl = iconUrl)
        }
    }

    fun createFamily() {
        val name = uiState.value.name.trim()
        val iconUrl = uiState.value.iconUrl.trim()

        if (name.isBlank() || iconUrl.isBlank()) {
            updateState {
                withMeta(meta.copy(errorMessage = "Name and icon URL must not be empty"))
            }
            return
        }

        if (iconUrl !in ALLOWED_ICON_URLS) {
            updateState {
                withMeta(meta.copy(errorMessage = "Invalid icon URL"))
            }
            return
        }

        viewModelScope.launch {
            updateState {
                withMeta(UiResourceMeta(isLoading = true, errorMessage = null))
            }

            val token = sessionRepository.getToken()
            if (token == null) {
                updateState {
                    withMeta(UiResourceMeta(errorMessage = "No active session"))
                }
                return@launch
            }

            runCatching {
                familyRepository.createFamily(token, name, iconUrl)
            }.onSuccess { createdFamily ->
                updateState {
                    copy(
                        meta = UiResourceMeta(),
                        name = "",
                        iconUrl = "",
                        createdFamily = createdFamily,
                    )
                }
            }.onFailure { throwable ->
                updateState {
                    withMeta(
                        UiResourceMeta(
                            errorMessage = throwable.message ?: "Failed to create family",
                        ),
                    )
                }
            }
        }
    }

    fun consumeCreatedFamily() {
        updateState {
            copy(createdFamily = null)
        }
    }
}
