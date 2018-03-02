package com.gx.morgan.imageloader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.gx.morgan.imageloaderlib.ImageLoader;

public class MainActivity extends AppCompatActivity {

    private ImageView ivImage;
    private static final String URL = "http://img5.imgtn.bdimg.com/it/u=2210270529,1470505196&fm=11&gp=0.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivImage = findViewById(R.id.iv_image);

        ImageLoader.display(URL,ivImage);
    }


}
