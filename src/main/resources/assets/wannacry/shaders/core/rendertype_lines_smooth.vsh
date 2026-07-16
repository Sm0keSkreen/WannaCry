#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec4 Color;
in vec3 Normal;
in float LineWidth;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out float lineEdge;

const float VIEW_SHRINK = 1.0 - (1.0 / 256.0);
const mat4 VIEW_SCALE = mat4(
    VIEW_SHRINK, 0.0, 0.0, 0.0,
    0.0, VIEW_SHRINK, 0.0, 0.0,
    0.0, 0.0, VIEW_SHRINK, 0.0,
    0.0, 0.0, 0.0, 1.0
);

void main() {
    // Normal must be a UNIT VECTOR in the same camera-relative space as Position.
    // Using a unit normal means Position+Normal is exactly 1 unit along the line —
    // guaranteed to be in front of the near plane and numerically stable for any
    // line length or viewing angle (fixes invisible tracers at long range).
    vec3 unitNormal = normalize(Normal);

    vec4 linePosStart = ProjMat * VIEW_SCALE * ModelViewMat * vec4(Position, 1.0);
    vec4 linePosEnd   = ProjMat * VIEW_SCALE * ModelViewMat * vec4(Position + unitNormal, 1.0);

    // Guard against degenerate projection (behind near plane)
    vec3 ndc1 = linePosStart.xyz / linePosStart.w;
    vec3 ndc2 = linePosEnd.w > 0.0 ? linePosEnd.xyz / linePosEnd.w : ndc1 + vec3(0.001, 0.0, 0.0);

    vec2 dir = (ndc2.xy - ndc1.xy) * ScreenSize;
    float dirLen = length(dir);
    if (dirLen < 0.0001) dir = vec2(1.0, 0.0);
    else dir /= dirLen;

    // Perpendicular offset in screen-space pixels, converted to NDC
    vec2 perp = vec2(-dir.y, dir.x) * LineWidth / ScreenSize;

    // Even vertex ID → +perp side, odd → -perp side
    float side = (gl_VertexID % 2 == 0) ? 1.0 : -1.0;
    vec2 offset = perp * side;

    gl_Position = vec4((ndc1 + vec3(offset, 0.0)) * linePosStart.w, linePosStart.w);

    lineEdge = side;

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color;
}
