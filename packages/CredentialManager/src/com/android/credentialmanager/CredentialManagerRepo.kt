/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.credentialmanager

import android.app.Activity
import android.app.slice.Slice
import android.app.slice.SliceSpec
import android.content.Context
import android.content.Intent
import android.credentials.ui.Constants
import android.credentials.ui.Entry
import android.credentials.ui.ProviderData
import android.credentials.ui.RequestInfo
import android.credentials.ui.UserSelectionResult
import android.graphics.drawable.Icon
import android.os.Binder
import android.os.Bundle
import android.os.ResultReceiver
import com.android.credentialmanager.createflow.CreatePasskeyUiState
import com.android.credentialmanager.createflow.CreateScreenState
import com.android.credentialmanager.getflow.GetCredentialUiState
import com.android.credentialmanager.getflow.GetScreenState

// Consider repo per screen, similar to view model?
class CredentialManagerRepo(
  private val context: Context,
  intent: Intent,
) {
  private val requestInfo: RequestInfo
  private val providerList: List<ProviderData>
  // TODO: require non-null.
  val resultReceiver: ResultReceiver?

  init {
    requestInfo = intent.extras?.getParcelable(
      RequestInfo.EXTRA_REQUEST_INFO,
      RequestInfo::class.java
    ) ?: RequestInfo(
      Binder(),
      RequestInfo.TYPE_CREATE,
      /*isFirstUsage=*/false
    )

    providerList = intent.extras?.getParcelableArrayList(
      ProviderData.EXTRA_PROVIDER_DATA_LIST,
      ProviderData::class.java
    ) ?: testProviderList()

    resultReceiver = intent.getParcelableExtra(
      Constants.EXTRA_RESULT_RECEIVER,
      ResultReceiver::class.java
    )
  }

  fun onCancel() {
    resultReceiver?.send(Activity.RESULT_CANCELED, null)
  }

  fun onOptionSelected(providerPackageName: String, entryId: Int) {
    val userSelectionResult = UserSelectionResult(
      requestInfo.token,
      providerPackageName,
      entryId
    )
    val resultData = Bundle()
    resultData.putParcelable(
      UserSelectionResult.EXTRA_USER_SELECTION_RESULT,
      userSelectionResult
    )
    resultReceiver?.send(Activity.RESULT_OK, resultData)
  }

  fun getCredentialInitialUiState(): GetCredentialUiState {
    val providerList = GetFlowUtils.toProviderList(providerList, context)
    return GetCredentialUiState(
      providerList,
      GetScreenState.CREDENTIAL_SELECTION,
      providerList.first()
    )
  }

  fun createPasskeyInitialUiState(): CreatePasskeyUiState {
    val providerList = CreateFlowUtils.toProviderList(providerList, context)
    return CreatePasskeyUiState(
      providers = providerList,
      currentScreenState = CreateScreenState.PASSKEY_INTRO,
    )
  }

  companion object {
    lateinit var repo: CredentialManagerRepo

    fun setup(
      context: Context,
      intent: Intent,
    ) {
      repo = CredentialManagerRepo(context, intent)
    }

    fun getInstance(): CredentialManagerRepo {
      return repo
    }
  }

  // TODO: below are prototype functionalities. To be removed for productionization.
  private fun testProviderList(): List<ProviderData> {
    return listOf(
      ProviderData.Builder(
        "com.google",
        "Google Password Manager",
        Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
        .setCredentialEntries(
          listOf<Entry>(
            newEntry(1, "elisa.beckett@gmail.com", "Elisa Backett",
                     "20 passwords and 7 passkeys saved"),
            newEntry(2, "elisa.work@google.com", "Elisa Backett Work",
                     "20 passwords and 7 passkeys saved"),
          )
        ).setActionChips(
          listOf<Entry>(
            newEntry(3, "Go to Settings", "",
                     "20 passwords and 7 passkeys saved"),
            newEntry(4, "Switch Account", "",
                     "20 passwords and 7 passkeys saved"),
          ),
        ).build(),
      ProviderData.Builder(
        "com.dashlane",
        "Dashlane",
        Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
        .setCredentialEntries(
          listOf<Entry>(
            newEntry(1, "elisa.beckett@dashlane.com", "Elisa Backett",
                     "20 passwords and 7 passkeys saved"),
            newEntry(2, "elisa.work@dashlane.com", "Elisa Backett Work",
                     "20 passwords and 7 passkeys saved"),
          )
        ).setActionChips(
          listOf<Entry>(
            newEntry(3, "Manage Accounts", "Manage your accounts in the dashlane app",
                     "20 passwords and 7 passkeys saved"),
          ),
        ).build(),
    )
  }

  private fun newEntry(id: Int, title: String, subtitle: String, usageData: String): Entry {
    val slice = Slice.Builder(
      Entry.CREDENTIAL_MANAGER_ENTRY_URI, SliceSpec(Entry.VERSION, 1)
    )
      .addText(title, null, listOf(Entry.HINT_TITLE))
      .addText(subtitle, null, listOf(Entry.HINT_SUBTITLE))
      .addIcon(
        Icon.createWithResource(context, R.drawable.ic_passkey),
        null,
        listOf(Entry.HINT_ICON))
      .addText(usageData, Slice.SUBTYPE_MESSAGE, listOf(Entry.HINT_SUBTITLE))
      .build()
    return Entry(
      id,
      slice
    )
  }
}
