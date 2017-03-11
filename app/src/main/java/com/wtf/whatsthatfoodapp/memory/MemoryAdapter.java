package com.wtf.whatsthatfoodapp.memory;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.wtf.whatsthatfoodapp.R;

public class MemoryAdapter extends FirebaseListAdapter<Memory> {

    private MemoryDao dao;

    public MemoryAdapter(Activity activity, Class<Memory> modelClass, Query
            query, MemoryDao dao) {
        super(activity, modelClass, R.layout.memory_list_item, query);
        this.dao = dao;
    }

    @Override
    protected void populateView(View v, Memory model, int position) {
        TextView viewTitle = (TextView) v.findViewById(R.id
                .memory_list_item_title);
        TextView viewLoc = (TextView) v.findViewById(R.id
                .memory_list_item_loc);
        ImageView viewImage = (ImageView) v.findViewById(R.id
                .memory_list_item_image);
        final ProgressBar progress = (ProgressBar) v.findViewById(R.id
                .memory_list_item_progress);

        viewTitle.setText(model.getTitle());
        viewLoc.setText(model.getLoc());

        // Load photo into view
        dao.loadImage(model)
                // Hide progress bar when image is loaded, or error occurs
                .listener(new RequestListener<StorageReference,
                        GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, StorageReference
                            model, Target<GlideDrawable> target, boolean
                            isFirstResource) {
                        progress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource,
                            StorageReference model, Target<GlideDrawable>
                            target, boolean isFromMemoryCache,
                            boolean isFirstResource) {
                        progress.setVisibility(View.GONE);
                        return false;
                    }
                })
                .centerCrop()
                .into(viewImage);
    }

}
