#include <stdio.h>
#include "odrSpiral/odrSpiral.h"
 
int main(int argc, char *argv[])
{
    fprintf(stdout, "start spiral_sampler\n");

    double cdot_min = -0.06777398710873976;
    double cdot_max = 0.012627715579067441;
    int cdot_steps = 100;
    double cdot_step_size = (cdot_max-cdot_min) / cdot_steps;
    fprintf(stdout, "cdot parameters: min: %.5f, max: %.5f, steps: %i, step_size: %.5f\n", cdot_min, cdot_max, cdot_steps, cdot_step_size);
    int i_cdot = 0;
    double cdot_values[cdot_steps];
    for (double cdot = cdot_min; cdot < cdot_max; cdot += cdot_step_size) {
        cdot_values[i_cdot] = cdot;
        i_cdot++;
    }
    // for selected values, use:
    //double cdot_values[] = {-0.051693646571178295};

    double s_min = -5463.268;
    double s_max = 38683.6;
    int s_steps = 10000;
    double s_step_size = (s_max-s_min) / s_steps;
    fprintf(stdout, "s parameters: min: %.5f, max: %.5f, steps: %i, step_size: %.5f\n", s_min, s_max, s_steps, s_step_size);
    int i_s = 0;
    double s_values[s_steps];
    for (double s = s_min; s < s_max; s += s_step_size) {
        s_values[i_s] = s;
        i_s++;
    }
    // for selected values, use:
    // double s_values[] = {6884.610979599647};


    double x, y, t;
    int count = 0;

    char filename[] = "sampled_spiral.csv";
    fprintf(stdout, "start writing sampled points to %s\n", filename);

    FILE *fpt;
    fpt = fopen(filename, "w+");
    fprintf( fpt, "cdot,s,x,y,t\n");


    for (i_cdot = 0; i_cdot < sizeof(cdot_values)/sizeof(double); i_cdot++)
    {
        for (i_s = 0; i_s < sizeof(s_values)/sizeof(double); i_s++)
        {
            odrSpiral( s_values[i_s], cdot_values[i_cdot], &x, &y, &t );
            fprintf( fpt, "%.17g,%.17g,%.17g,%.17g,%.17g\n", cdot_values[i_cdot], s_values[i_s], x, y, t);
            count++;
        }
    }
    fprintf(stdout, "wrote %i sample points\n", count);

    return 0;
}
