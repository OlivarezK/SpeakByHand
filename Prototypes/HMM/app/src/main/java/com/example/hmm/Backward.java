package com.example.hmm;
import static java.lang.System.*;

public class Backward {
    public static double compute(int obs[], int states[], double start_prob[], double trans_prob[][], double emiss_prob[][]) throws ArrayIndexOutOfBoundsException
    {
        double backward[][] = new double[states.length][obs.length];
        double prob = 0.0;

        out.println("The observation sequence - ");
        for(int i=0; i<obs.length; i++)
        {
            out.print(obs[i]+" ");
        }
        out.println();

        // Initializing the Backward Matrix

        for(int i=0;i<states.length;i++)
        {
            backward[i][obs.length -1]= 1;

        }

        for(int t = obs.length-2; t >= 0; t--)
        {

            for(int i = 0; i < states.length; i++)
            {

                backward[i][t] = 0;

                for(int j = 0; j < states.length; j++)
                {

                    prob += (backward[j][t+1] * trans_prob[i][j] * emiss_prob[j][obs[t+1]]);

                }


            }
        }
        return prob;

    }
}
