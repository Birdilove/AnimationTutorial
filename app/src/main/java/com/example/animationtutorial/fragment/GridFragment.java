/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.animationtutorial.fragment;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.SharedElementCallback;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.animationtutorial.MainActivity;
import com.example.animationtutorial.R;
import com.example.animationtutorial.adapter.CustomAdapter;
import java.util.List;
import java.util.Map;


public class GridFragment extends Fragment {

  private RecyclerView recyclerView;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_grid, container, false);
    recyclerView.setAdapter(new CustomAdapter(this));

    prepareTransitions();
    postponeEnterTransition();

    return recyclerView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    scrollToPosition();
  }

  private void scrollToPosition() {
    recyclerView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v,
          int left,
          int top,
          int right,
          int bottom,
          int oldLeft,
          int oldTop,
          int oldRight,
          int oldBottom) {
        recyclerView.removeOnLayoutChangeListener(this);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        View viewAtPosition = layoutManager.findViewByPosition(MainActivity.currentPosition);
        if (viewAtPosition == null || layoutManager
            .isViewPartiallyVisible(viewAtPosition, false, true)) {
          recyclerView.post(() -> layoutManager.scrollToPosition(MainActivity.currentPosition));
        }
      }
    });
  }

  private void prepareTransitions() {
    setExitTransition(TransitionInflater.from(getContext())
        .inflateTransition(R.transition.grid_exit_transition));
    setExitSharedElementCallback(
        new SharedElementCallback() {
          @Override
          public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            // Locate the ViewHolder for the clicked position.
            RecyclerView.ViewHolder selectedViewHolder = recyclerView
                .findViewHolderForAdapterPosition(MainActivity.currentPosition);
            if (selectedViewHolder == null) {
              return;
            }
            sharedElements
                .put(names.get(0), selectedViewHolder.itemView.findViewById(R.id.image));
          }
        });
  }
}
