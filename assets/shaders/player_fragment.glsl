
varying vec4 v_color;
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform mat4 u_projTrans;

//custom uniforms used as flags
uniform int hasArmor;
uniform int hasHolyLance;
uniform int isConfused;

//Walfrit rgb values
float Walfrit_red_r = 237.0/255.0;
float Walfrit_red_g = 28.0/255.0;
float Walfrit_red_b = 36.0/255.0;
float Walfrit_yellow_r = 255.0/255.0;
float Walfrit_yellow_g = 219.0/255.0;
float Walfrit_yellow_b = 92.0/255.0;
float Walfrit_pink_r = 255.0/255.0;
float Walfrit_pink_g = 174.0/255.0;
float Walfrit_pink_b = 201.0/255.0;

vec3 Walfrit_red_color = vec3(Walfrit_red_r, Walfrit_red_g, Walfrit_red_b);
vec3 Walfrit_yellow_color = vec3(Walfrit_yellow_r, Walfrit_yellow_g, Walfrit_yellow_b);
vec3 Walfrit_pink_color = vec3(Walfrit_pink_r, Walfrit_pink_g, Walfrit_pink_b);


void main() {

    //extract current pixel color
    vec4 color = texture2D(u_texture, v_texCoords).rgba;

    //If has armor, the red is shifted to gray
    if (hasArmor == 1 && color.xyz == Walfrit_red_color){
        color.xyz=vec3(0.5, 0.5, 0.5);
    } else if (hasHolyLance == 0 && color.xyz == Walfrit_yellow_color){
        //If holy lance hasn't been found, the yellow is set to black
        color.xyz=vec3(0,0,0);
    } else if (isConfused == 1 && color.xyz == Walfrit_pink_color){
        //If player is confused, skin goes white
        color.xyz=vec3(1,1,1);
    }

    //Set colors, with modifications if present
    gl_FragColor = color;
}
