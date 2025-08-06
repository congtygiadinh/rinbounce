/*
 * RinBounce Beautiful Ocean Background Shader
 * Enhanced gradient matching GuiMainMenu ocean theme
 * Smooth animated transitions with wave effects
 */
#version 120

#ifdef GL_ES
precision lowp float;
#endif

uniform float iTime;
uniform vec2 iResolution;

// Smooth noise function for wave effects
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(hash(i + vec2(0.0, 0.0)), hash(i + vec2(1.0, 0.0)), u.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

// Multi-layer ocean waves
float oceanWaves(vec2 uv, float time) {
    // Primary wave layers
    float wave1 = sin(uv.x * 8.0 + time * 1.2) * 0.04;
    float wave2 = sin(uv.x * 12.0 - time * 0.8) * 0.03;
    float wave3 = sin(uv.x * 16.0 + time * 1.5) * 0.025;

    // Secondary ripple effects
    float ripple1 = sin(uv.x * 20.0 + time * 2.0) * 0.015;
    float ripple2 = sin(uv.x * 24.0 - time * 1.8) * 0.012;

    // Combine with depth-based intensity
    float depth = smoothstep(0.0, 0.3, uv.y) * smoothstep(1.0, 0.7, uv.y);
    return (wave1 + wave2 + wave3 + ripple1 + ripple2) * depth;
}

// Beautiful animated ocean gradient
vec3 createOceanGradient(vec2 uv, float time) {
    // Smooth animation time
    float animTime = time * 0.4;

    // Ocean color layers (matching GuiMainMenu)
    vec3 deepOcean = vec3(
        0.16 + 0.15 * sin(animTime),           // Deep blue base
        0.31 + 0.20 * sin(animTime * 0.7),     // Medium blue
        0.47 + 0.25 * cos(animTime * 0.8)      // Light blue
    );

    vec3 midOcean = vec3(
        0.31 + 0.25 * sin(animTime * 1.2),     // Cyan tones
        0.55 + 0.30 * cos(animTime * 0.9),     // Light cyan
        0.71 + 0.35 * sin(animTime * 1.1)      // Very light cyan
    );

    vec3 surface = vec3(
        0.59 + 0.35 * sin(animTime * 0.8),     // Light blue
        0.78 + 0.25 * cos(animTime),           // Almost white
        0.94 + 0.08 * sin(animTime * 1.3)      // Pure white
    );

    // Smooth gradient transitions
    float gradient1 = smoothstep(0.0, 0.45, uv.y);
    float gradient2 = smoothstep(0.35, 0.85, uv.y);

    // Mix colors with smooth blending
    vec3 color = mix(deepOcean, midOcean, gradient1);
    color = mix(color, surface, gradient2);

    return color;
}

// Add atmospheric depth and lighting
vec3 addAtmosphere(vec3 baseColor, vec2 uv, float time) {
    // Subtle atmospheric noise
    float atmosphere = noise(uv * 3.0 + time * 0.1) * 0.06;

    // Light atmospheric color
    vec3 atmosphereColor = vec3(0.85, 0.92, 0.98);

    // Apply atmosphere effect stronger near surface
    float atmosphereStrength = smoothstep(0.5, 1.0, uv.y) * 0.3;

    return mix(baseColor, atmosphereColor, atmosphere * atmosphereStrength);
}

// Surface shimmer and light effects
vec3 addShimmer(vec3 baseColor, vec2 uv, float time) {
    // Multiple shimmer layers
    float shimmer1 = sin(uv.x * 16.0 + time * 2.5) * sin(uv.y * 12.0 + time * 1.8);
    float shimmer2 = sin(uv.x * 24.0 - time * 3.0) * sin(uv.y * 18.0 - time * 2.2);

    // Combine shimmers
    float totalShimmer = (shimmer1 + shimmer2) * 0.015;

    // Apply shimmer stronger near surface
    float shimmerStrength = smoothstep(0.6, 1.0, uv.y);
    totalShimmer *= shimmerStrength;

    // Add shimmer with blue-white tint
    vec3 shimmerColor = vec3(0.8, 0.9, 1.0);
    return baseColor + shimmerColor * totalShimmer;
}

void main() {
    vec2 uv = gl_FragCoord.xy / iResolution.xy;

    // Create wave distortion
    float waves = oceanWaves(uv, iTime);
    vec2 distortedUV = vec2(uv.x, uv.y + waves);

    // Create base ocean gradient
    vec3 color = createOceanGradient(distortedUV, iTime);

    // Add atmospheric effects
    color = addAtmosphere(color, uv, iTime);

    // Add surface shimmer
    color = addShimmer(color, uv, iTime);

    // Subtle depth-based lighting
    float lighting = 1.0 - smoothstep(0.0, 0.8, uv.y) * 0.15;
    color *= lighting;

    // Add very subtle noise texture
    float texture = noise(uv * 8.0 + iTime * 0.05) * 0.02;
    color += vec3(texture * 0.5, texture * 0.7, texture);

    // Final color correction
    color = pow(color, vec3(0.95)); // Slight gamma correction
    color = clamp(color, 0.0, 1.0);

    gl_FragColor = vec4(color, 1.0);
}