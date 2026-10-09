package com.example.luxevistaresortapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Centralized helper for Android 15 & 16 Edge-to-Edge compliance, WindowInsets handling,
 * and unified luxury bottom navigation bar setup with safe system navigation insets.
 */
public class EdgeToEdgeHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_SERVICES = 1;
    public static final int TAB_ATTRACTIONS = 2;
    public static final int TAB_PROFILE = 3;

    public static final int COLOR_GOLD_ACTIVE = Color.parseColor("#C5A059");
    public static final int COLOR_INACTIVE = Color.parseColor("#94A3B8");

    /**
     * Enables edge-to-edge window decor across Android versions.
     */
    public static void enableEdgeToEdge(ComponentActivity activity) {
        if (activity != null) {
            EdgeToEdge.enable(activity);
        }
    }

    /**
     * Applies system navigation bar insets to the bottom navigation bar container.
     * Prevents double-padding on subsequent layout passes.
     */
    public static void applyBottomNavInsets(View bottomNavContainer) {
        if (bottomNavContainer == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavContainer, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    /**
     * Applies status bar top insets to a top header or toolbar view.
     */
    public static void applyTopInset(View headerView) {
        if (headerView == null) return;
        final int initialPaddingTop = headerView.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(headerView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(v.getPaddingLeft(), initialPaddingTop + insets.top, v.getPaddingRight(), v.getPaddingBottom());
            return windowInsets;
        });
    }

    /**
     * Applies system bars insets (status bar top, navigation bar bottom, display cutout) to a scroll container or root view.
     */
    public static void applyRootWindowInsets(View rootView) {
        if (rootView == null) return;
        final int initialLeft = rootView.getPaddingLeft();
        final int initialTop = rootView.getPaddingTop();
        final int initialRight = rootView.getPaddingRight();
        final int initialBottom = rootView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(
                    initialLeft + insets.left,
                    initialTop + insets.top,
                    initialRight + insets.right,
                    initialBottom + insets.bottom
            );
            return windowInsets;
        });
    }

    /**
     * Applies bottom navigation bar insets to a ScrollView or ListView content so bottom items are never obscured.
     */
    public static void applyBottomInset(View scrollableView) {
        if (scrollableView == null) return;
        final int initialBottom = scrollableView.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(scrollableView, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    initialBottom + insets.bottom
            );
            return windowInsets;
        });
    }

    /**
     * Configures the luxury bottom navigation bar across Home, Services, Attractions, and Profile screens.
     */
    public static void setupBottomNavigation(Activity activity, int activeTab) {
        if (activity == null) return;

        View container = activity.findViewById(R.id.bottomNavigationContainer);
        if (container != null) {
            applyBottomNavInsets(container);
        }

        View tabHome = activity.findViewById(R.id.tabHome);
        View tabServices = activity.findViewById(R.id.tabServices);
        View tabAttractions = activity.findViewById(R.id.tabAttractions);
        View tabProfile = activity.findViewById(R.id.tabProfile);

        ImageView navHome = activity.findViewById(R.id.navHome);
        ImageView navServices = activity.findViewById(R.id.navServices);
        ImageView navAttractions = activity.findViewById(R.id.navAttractions);
        ImageView navProfile = activity.findViewById(R.id.navProfile);

        TextView tvNavHome = activity.findViewById(R.id.tvNavHome);
        TextView tvNavServices = activity.findViewById(R.id.tvNavServices);
        TextView tvNavAttractions = activity.findViewById(R.id.tvNavAttractions);
        TextView tvNavProfile = activity.findViewById(R.id.tvNavProfile);

        // Apply Gold active color and Muted inactive color
        styleTab(navHome, tvNavHome, activeTab == TAB_HOME);
        styleTab(navServices, tvNavServices, activeTab == TAB_SERVICES);
        styleTab(navAttractions, tvNavAttractions, activeTab == TAB_ATTRACTIONS);
        styleTab(navProfile, tvNavProfile, activeTab == TAB_PROFILE);

        // Navigation Actions
        View.OnClickListener homeListener = v -> {
            if (activeTab != TAB_HOME) {
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        };
        if (tabHome != null) tabHome.setOnClickListener(homeListener);
        if (navHome != null) navHome.setOnClickListener(homeListener);

        View.OnClickListener servicesListener = v -> {
            if (activeTab != TAB_SERVICES) {
                Intent intent = new Intent(activity, ServicesActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        };
        if (tabServices != null) tabServices.setOnClickListener(servicesListener);
        if (navServices != null) navServices.setOnClickListener(servicesListener);

        View.OnClickListener attractionsListener = v -> {
            if (activeTab != TAB_ATTRACTIONS) {
                Intent intent = new Intent(activity, AttractionsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        };
        if (tabAttractions != null) tabAttractions.setOnClickListener(attractionsListener);
        if (navAttractions != null) navAttractions.setOnClickListener(attractionsListener);

        View.OnClickListener profileListener = v -> {
            if (activeTab != TAB_PROFILE) {
                Intent intent = new Intent(activity, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        };
        if (tabProfile != null) tabProfile.setOnClickListener(profileListener);
        if (navProfile != null) navProfile.setOnClickListener(profileListener);
    }

    private static void styleTab(ImageView icon, TextView label, boolean isActive) {
        int color = isActive ? COLOR_GOLD_ACTIVE : COLOR_INACTIVE;
        if (icon != null) {
            icon.setColorFilter(color);
        }
        if (label != null) {
            label.setTextColor(color);
        }
    }
}
