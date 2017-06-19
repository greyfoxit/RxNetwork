/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.rxnetwork;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import greyfox.rxnetwork.RxNetwork;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import toothpick.Toothpick;

/**
 * Base RxNetwork fragment class.
 *
 * @author Radek Kozak
 */
public abstract class RxNetworkFragment extends Fragment {

  @BindView(R.id.internetInfo) TextView internetInfo;
  @BindView(R.id.fragmentName) TextView fragmentName;

  private CompositeDisposable subscriptions;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    Toothpick.inject(this, Toothpick.openScopes(RxNetwork.class, this));

    View view = inflater.inflate(R.layout.fragment_rxnework, container, false);

    ButterKnife.bind(this, view);

    fragmentName.setText(getFragmentName());

    subscriptions = new CompositeDisposable();

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    subscriptions.add(subscription());
  }

  @Override
  public void onPause() {
    super.onPause();
    subscriptions.clear();
  }

  void onError(@NonNull Throwable throwable) {
    Log.d(getFragmentName(), "onError: " + throwable.getMessage());
  }

  void toastInternetConnection(Boolean connected) {
    internetInfo.setText(getString(R.string.internet_info, connected));
  }

  abstract String getFragmentName();

  @NonNull
  abstract Disposable subscription();
}
