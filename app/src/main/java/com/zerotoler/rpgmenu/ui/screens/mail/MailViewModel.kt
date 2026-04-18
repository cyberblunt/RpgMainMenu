package com.zerotoler.rpgmenu.ui.screens.mail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zerotoler.rpgmenu.data.repository.PlayerProgressRepository
import com.zerotoler.rpgmenu.data.service.MailService
import com.zerotoler.rpgmenu.domain.model.rewards.RewardBundle
import com.zerotoler.rpgmenu.domain.usecase.RewardGrantUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MailViewModel(
    private val mailService: MailService,
    private val playerProgressRepository: PlayerProgressRepository,
    private val rewardGrantUseCase: RewardGrantUseCase,
) : ViewModel() {

    val mailState: StateFlow<com.zerotoler.rpgmenu.domain.model.progress.MailProgress> =
        mailService.observeMail().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = com.zerotoler.rpgmenu.domain.model.progress.MailProgress(),
        )

    fun markRead(id: String) {
        viewModelScope.launch {
            mailService.updateMail { m ->
                m.copy(
                    messages = m.messages.map {
                        if (it.id == id) it.copy(read = true) else it
                    },
                )
            }
        }
    }

    fun claim(id: String) {
        viewModelScope.launch {
            val current = mailService.observeMail().first()
            val msg = current.messages.find { it.id == id } ?: return@launch
            if (msg.claimed) return@launch
            if (!playerProgressRepository.tryMarkMailClaimProcessed("mail_claim_$id")) return@launch
            rewardGrantUseCase(
                RewardBundle(gold = msg.goldReward, gems = msg.gemReward),
            )
            mailService.updateMail { m ->
                m.copy(
                    messages = m.messages.map {
                        if (it.id == id) it.copy(claimed = true, read = true) else it
                    },
                )
            }
        }
    }
}
