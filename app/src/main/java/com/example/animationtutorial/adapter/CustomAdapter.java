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

package com.example.animationtutorial.adapter;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.transition.TransitionSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.animationtutorial.MainActivity;
import com.example.animationtutorial.R;
import com.example.animationtutorial.adapter.CustomAdapter.ImageViewHolder;
import com.example.animationtutorial.fragment.ImagePagerFragment;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.example.animationtutorial.adapter.ImageData.IMAGE_DRAWABLES;

public class CustomAdapter extends RecyclerView.Adapter<ImageViewHolder> {

  private interface ViewHolderListener {
    void onLoadCompleted(ImageView view, int adapterPosition);
    void onItemClicked(View view, int adapterPosition);
  }

  private final RequestManager requestManager;
  private final ViewHolderListener viewHolderListener;
  public CustomAdapter(Fragment fragment) {
    this.requestManager = Glide.with(fragment);
    this.viewHolderListener = new ViewHolderListenerImpl(fragment);
  }

  @NonNull
  @Override
  public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.image_card, parent, false);
    return new ImageViewHolder(view, requestManager, viewHolderListener);
  }

  @Override
  public void onBindViewHolder(ImageViewHolder holder, int position) {
    holder.onBind();
  }

  @Override
  public int getItemCount() {
    return IMAGE_DRAWABLES.length;
  }

  private static class ViewHolderListenerImpl implements ViewHolderListener {

    private Fragment fragment;
    private AtomicBoolean enterTransitionStarted;

    ViewHolderListenerImpl(Fragment fragment) {
      this.fragment = fragment;
      this.enterTransitionStarted = new AtomicBoolean();
    }

    @Override
    public void onLoadCompleted(ImageView view, int position) {
      if (MainActivity.currentPosition != position) {
        return;
      }
      if (enterTransitionStarted.getAndSet(true)) {
        return;
      }
      fragment.startPostponedEnterTransition();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClicked(View view, int position) {
      MainActivity.currentPosition = position;
      ((TransitionSet) fragment.getExitTransition()).excludeTarget(view, true);
      ImageView transitioningView = view.findViewById(R.id.image);
      fragment.getFragmentManager()
          .beginTransaction()
          .setReorderingAllowed(true)
          .addSharedElement(transitioningView, transitioningView.getTransitionName())
          .replace(R.id.fragment_container, new ImagePagerFragment(), ImagePagerFragment.class
              .getSimpleName())
          .addToBackStack(null)
          .commit();
    }
  }

  static class ImageViewHolder extends RecyclerView.ViewHolder implements
      View.OnClickListener {

    private final ImageView image;
    private final RequestManager requestManager;
    private final ViewHolderListener viewHolderListener;

    ImageViewHolder(View itemView, RequestManager requestManager,
        ViewHolderListener viewHolderListener) {
      super(itemView);
      this.image = itemView.findViewById(R.id.image);
      this.requestManager = requestManager;
      this.viewHolderListener = viewHolderListener;
      itemView.findViewById(R.id.card_view).setOnClickListener(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void onBind() {
      int adapterPosition = getAdapterPosition();
      setImage(adapterPosition);
      image.setTransitionName(String.valueOf(IMAGE_DRAWABLES[adapterPosition]));
    }

    void setImage(final int adapterPosition) {
      requestManager
          .load(IMAGE_DRAWABLES[adapterPosition])
          .listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                        Target<Drawable> target, boolean isFirstResource) {
              viewHolderListener.onLoadCompleted(image, adapterPosition);
              return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable>
                target, DataSource dataSource, boolean isFirstResource) {
              viewHolderListener.onLoadCompleted(image, adapterPosition);
              return false;
            }
          })
          .into(image);
    }

    @Override
    public void onClick(View view) {
      viewHolderListener.onItemClicked(view, getAdapterPosition());
    }
  }

}