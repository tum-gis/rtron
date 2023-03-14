#include <stdio.h>

#include "odrSpiral/odrSpiral.h"

int main(int argc, char *argv[]) {
    fprintf(stdout, "start fresnel_integral_sampler\n");

    double l_min = -0.2 - 1.0/7;
    double l_max = 0.2 + 1.0/7;
    int l_steps = 200000;
    double l_step_size = (l_max-l_min) / l_steps;
    fprintf(stdout, "l parameters: min: %.5f, max: %.5f, steps: %i, step_size: %.5f\n", l_min, l_max, l_steps, l_step_size);

    double l_values[l_steps];
    int i_l = 0;
    for (double l = l_min; l < l_max; l += l_step_size) {
        l_values[i_l] = l;
        i_l++;
    }
    // for selected values, use:
    // double l_values[] = {-4.228402886795016, 883.1267776797073, -1.8154077322757265};


    double l, x, y;
    int count = 0;

    char filename[] = "sampled_fresnel_integral.csv";
    fprintf(stdout, "start writing sampled points to %s\n", filename);
    FILE *fpt;
    fpt = fopen(filename, "w+");
    fprintf( fpt, "l,x,y\n");


    for (i_l = 0; i_l < sizeof(l_values)/sizeof(double); i_l++)
    {
        fresnel( l_values[i_l], &y, &x );
        fprintf( fpt, "%.17g,%.17g,%.17g\n", l_values[i_l], x, y);
        count++;
    }
    fprintf(stdout, "wrote %i sample points\n", count);
}
