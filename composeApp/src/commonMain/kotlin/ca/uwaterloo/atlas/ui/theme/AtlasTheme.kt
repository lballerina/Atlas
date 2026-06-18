package ca.uwaterloo.atlas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand palette ──────────────────────────────────────────────────────────
//
// Every value here corresponds to a colour that is used by name throughout
// the codebase.  The goal is that all screen/component files can write
// MaterialTheme.colorScheme.primary (etc.) instead of a raw hex literal,
// making future re-brands a one-file change.
//
// Gradient colours (GradStart / GradMid / GradEnd) appear in header boxes
// on every main screen.  They are exposed as top-level constants so that any
// file can import them directly when it needs to draw a gradient — Compose's
// Brush API does not accept MaterialTheme tokens.
// ──────────────────────────────────────────────────────────────────────────

/** Indigo-blue: buttons, links, active icons, accent text. */
val AtlasPrimary = Color(0xFF4C5BD4)

/** Pale indigo: chip / badge backgrounds, tinted surfaces. */
val AtlasPrimaryContainer = Color(0xFFF0F3FF)

/** Dark on-primary text (used inside filled buttons / on white cards). */
val AtlasOnPrimary = Color.White

/** Deep text colour for headings & body copy. */
val AtlasOnSurface = Color(0xFF1F1F1F)

/** Mid-grey used for secondary labels, metadata, placeholders. */
val AtlasOnSurfaceVariant = Color(0xFF6B7280)

/** Lightest grey used for inactive chips, input containers. */
val AtlasSurfaceVariant = Color(0xFFF3F4F6)

/** App-wide page background. */
val AtlasBackground = Color(0xFFF7F7FB)

/** Card / sheet background. */
val AtlasSurface = Color.White

/** Destructive action colour. */
val AtlasError = Color(0xFFEF4444)

/** Success / money-green. */
val AtlasSuccess = Color(0xFF10B981)

/** Gradient stop 1 — indigo */
val AtlasGradStart = Color(0xFF4C5BD4)

/** Gradient stop 2 — mauve */
val AtlasGradMid = Color(0xFF8D6E95)

/** Gradient stop 3 — dusty rose */
val AtlasGradEnd = Color(0xFFC79AA1)

// ── Material 3 colour scheme ───────────────────────────────────────────────
//
// Atlas only uses light mode for now.  A dark scheme can be added later by
// duplicating this block with darker surface / background values.
// ──────────────────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    // ── Primary ──────────────────────────────────────────────────────────
    primary            = AtlasPrimary,
    onPrimary          = AtlasOnPrimary,
    primaryContainer   = AtlasPrimaryContainer,
    onPrimaryContainer = AtlasPrimary,

    // ── Secondary (reuses brand tints for now) ────────────────────────
    secondary            = Color(0xFF8D6E95),     // mauve gradient mid
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFF5EEF8),
    onSecondaryContainer = Color(0xFF5A3D6B),

    // ── Tertiary (success-green, used for price indicators) ───────────
    tertiary            = AtlasSuccess,
    onTertiary          = Color.White,
    tertiaryContainer   = Color(0xFFD1FAE5),
    onTertiaryContainer = Color(0xFF065F46),

    // ── Error (destructive actions) ───────────────────────────────────
    error            = AtlasError,
    onError          = Color.White,
    errorContainer   = Color(0xFFFFE4E4),
    onErrorContainer = Color(0xFF7F1D1D),

    // ── Backgrounds & surfaces ────────────────────────────────────────
    background          = AtlasBackground,
    onBackground        = AtlasOnSurface,
    surface             = AtlasSurface,
    onSurface           = AtlasOnSurface,
    surfaceVariant      = AtlasSurfaceVariant,
    onSurfaceVariant    = AtlasOnSurfaceVariant,

    // ── Borders / outlines ────────────────────────────────────────────
    outline        = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFD1D5DB),
)

/**
 * Atlas Travel App Theme.
 *
 * Wrap the entire app (and every Compose preview) in this theme.
 * All screens and components should use [MaterialTheme.colorScheme]
 * tokens instead of raw hex literals; see [AtlasPrimary] etc. above
 * for the canonical colour values.
 */
@Composable
fun AtlasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content     = content
    )
}
