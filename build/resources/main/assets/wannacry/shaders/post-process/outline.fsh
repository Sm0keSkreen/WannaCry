#version 330 core

in vec2 v_TexCoord;
in vec2 v_OneTexel;

uniform sampler2D u_Texture;

layout (std140) uniform OutlineData {
    int width;
    float fillOpacity;
    int shapeMode;
    float glowMultiplier;
} u_Outline;

out vec4 color;

void main() {
    vec4 center = texture(u_Texture, v_TexCoord);

    if (center.a != 0.0) {
        if (u_Outline.shapeMode == 0) discard;
        center = vec4(center.rgb, center.a * u_Outline.fillOpacity);
    } else {
        if (u_Outline.shapeMode == 1) discard;

        float minDist = float(u_Outline.width * u_Outline.width);
        float dist    = minDist * 4.0;    // sentinel: far outside the glow range
        vec4  found   = vec4(0.0);

        // Scan only pixels within the circle (skip corners to cut ~21 % of samples
        // vs. a full square scan while producing identical visual output because
        // corner samples are always beyond minDist and never affect the result).
        float wf = float(u_Outline.width);
        for (int x = -u_Outline.width; x <= u_Outline.width; x++) {
            float xf   = float(x);
            float xSq  = xf * xf;
            // Skip this column entirely if its nearest point is already beyond the
            // glow radius – this turns the square scan into a circle scan.
            if (xSq > minDist) continue;

            for (int y = -u_Outline.width; y <= u_Outline.width; y++) {
                float yf  = float(y);
                float ySq = yf * yf;
                if (xSq + ySq > minDist) continue;    // outside the circle

                vec4 s = texture(u_Texture, v_TexCoord + v_OneTexel * vec2(xf, yf));
                if (s.a != 0.0) {
                    float nd = xSq + ySq - 1.0;
                    if (nd < dist) {
                        dist  = nd;
                        found = s;
                        // Early-out: we already have a pixel at distance ≤ 0 (i.e.
                        // directly adjacent or overlapping), so the alpha will be
                        // clamped to glowMultiplier anyway – no point sampling more.
                        if (dist <= 0.0) break;
                    }
                }
            }
            if (dist <= 0.0) break;    // matched the outer loop too
        }

        if (dist > minDist) {
            discard;
        } else {
            center   = found;
            center.a = min((1.0 - (dist / minDist)) * u_Outline.glowMultiplier, 1.0);
        }
    }

    color = center;
}
