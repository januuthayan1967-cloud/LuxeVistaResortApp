package com.example.luxevistaresortapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Centralized helper for Android 15 & 16 Edge-to-Edge compliance and WindowInsets handling.
 * Ensures the bottom navigation bar and interactive content are never obscured by Samsung
 * 3-button system navigation, Android gesture pill, or status bars.
 */
public class EdgeToEdgeHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_SERVICES = 1;
    public static final int TAB_ATTRACTIONS = 2;
    public static final int TAB_PROFILE = 3;

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

        ImageButton navHome = activity.findViewById(R.id.navHome);
        ImageButton navServices = activity.findViewById(R.id.navServices);
        ImageButton navAttractions = activity.findViewById(R.id.navAttractions);
        ImageButton navProfile = activity.findViewById(R.id.navProfile);

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                if (activeTab != TAB_HOME) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navServices != null) {
            navServices.setOnClickListener(v -> {
                if (activeTab != TAB_SERVICES) {
                    Intent intent = new Intent(activity, ServicesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navAttractions != null) {
            navAttractions.setOnClickListener(v -> {
                if (activeTab != TAB_ATTRACTIONS) {
                    Intent intent = new Intent(activity, AttractionsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (activeTab != TAB_PROFILE) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(intent);
                }
            });
        }
    }
}
