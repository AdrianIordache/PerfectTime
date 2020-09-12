package com.example.adi.perfecttime;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
// First Time Backup
/*public class RecycleViewAdapterPosts extends RecyclerView.Adapter<RecycleViewAdapterPosts.ViewHolder>
{

    Context context;
    List<Posts> Array;

    public RecycleViewAdapterPosts(Context context, List<Posts> TempList)
    {
        this.Array = TempList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

        Posts posts = Array.get(position);

        String PostKey = posts.getUserID() + posts.getDate() + posts.getTime();

        holder.Date.setText(" " + posts.getDate());
        holder.Time.setText(" " + posts.getTime());
        holder.FullName.setText(posts.getFullName());
        holder.PostDescription.setText(posts.getPostDescription());

        Picasso.get().load(posts.getProfileImage()).into(holder.ProfileImage);
        Picasso.get().load(posts.getImage()).into(holder.Image);

    }

    @Override
    public int getItemCount() {

        return Array.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView Date, FullName, PostDescription , Time;
        public CircleImageView ProfileImage;
        public ImageView Image;

        public ViewHolder(View itemView) {
            super(itemView);

            Date = (TextView) itemView.findViewById(R.id.post_date);
            Time = (TextView) itemView.findViewById(R.id.post_time);
            FullName = (TextView) itemView.findViewById(R.id.post_user_name);
            PostDescription = (TextView) itemView.findViewById(R.id.post_description);
            ProfileImage = (CircleImageView) itemView.findViewById(R.id.post_profile_image);
            Image = (ImageView) itemView.findViewById(R.id.post_image);


        }
    }
}*/
