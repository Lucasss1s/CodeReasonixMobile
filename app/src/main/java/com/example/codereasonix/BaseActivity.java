package com.example.codereasonix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final int MENU_PERFIL             = 1;
    protected static final int MENU_MIS_DESAFIOS       = 2;
    protected static final int MENU_MIS_POSTULACIONES  = 3;
    protected static final int MENU_LOGOUT             = 4;

    protected void setupTopBar() {
        ImageView imgAvatarTop = findViewById(R.id.imgAvatarTop);

        if (imgAvatarTop != null) {
            actualizarAvatarTopBar(imgAvatarTop);

            imgAvatarTop.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(BaseActivity.this, v);

                popup.getMenu().add(0, MENU_PERFIL,             0, "Perfil");
                popup.getMenu().add(0, MENU_MIS_DESAFIOS,       1, "Mis desafíos");
                popup.getMenu().add(0, MENU_MIS_POSTULACIONES,  2, "Mis postulaciones");
                popup.getMenu().add(0, MENU_LOGOUT,             3, "Cerrar sesión");

                popup.setOnMenuItemClickListener(this::onMenuItemSelected);
                popup.show();
            });
        }
    }

    private void actualizarAvatarTopBar(ImageView imgAvatarTop) {
        SharedPreferences prefs = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
        String avatarUrl = prefs.getString("avatar_url", "");

        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(this)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_avatar_default)
                    .error(R.drawable.ic_avatar_default)
                    .circleCrop()
                    .into(imgAvatarTop);
        } else {
            imgAvatarTop.setImageResource(R.drawable.ic_avatar_default);
        }
    }

    protected void enableImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            View decorView = getWindow().getDecorView();
            WindowInsetsController insetsController = decorView.getWindowInsetsController();
            if (insetsController != null) {
                insetsController.hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars()
                );
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            View decorView = getWindow().getDecorView();
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(flags);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        enableImmersiveMode();

        ImageView imgAvatarTop = findViewById(R.id.imgAvatarTop);
        if (imgAvatarTop != null) {
            actualizarAvatarTopBar(imgAvatarTop);
        }
    }

    protected void setupBottomNav() {
        LinearLayout btnNavHome        = findViewById(R.id.btnNavHome);
        LinearLayout btnNavEntrevistas = findViewById(R.id.btnNavEntrevistas);

        if (btnNavHome != null) {
            btnNavHome.setOnClickListener(v -> {
                if (this instanceof HomeActivity) {
                    return;
                }
                Intent i = new Intent(this, HomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            });
        }

        if (btnNavEntrevistas != null) {
            btnNavEntrevistas.setOnClickListener(v -> {
                if (this instanceof OfertasActivity) return;
                Intent i = new Intent(this, OfertasActivity.class);
                startActivity(i);
            });
        }
    }

    protected boolean onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == MENU_PERFIL) {
            if (!(this instanceof PerfilActivity)) {
                startActivity(new Intent(this, PerfilActivity.class));
            }
            return true;
        } else if (id == MENU_MIS_DESAFIOS) {
            if (!(this instanceof MisDesafiosActivity)) {
                startActivity(new Intent(this, MisDesafiosActivity.class));
            }
            return true;
        } else if (id == MENU_MIS_POSTULACIONES) {
            if (!(this instanceof MisPostulacionesActivity)) {
                startActivity(new Intent(this, MisPostulacionesActivity.class));
            }
            return true;
        } else if (id == MENU_LOGOUT) {
            SharedPreferences prefs1 = getSharedPreferences("CodeReasonixPrefs", MODE_PRIVATE);
            prefs1.edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
