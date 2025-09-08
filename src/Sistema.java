import java.util.Random;

public class Sistema {
    public static void main(String[] args) {

        Random random = new Random();
        
        final int cambioContexto = 1; //Establecer el cambio de contexto
        final int numeroColas = 2; //Establecer el numero de colas
        final int quantum;
        final int numeroProcesos;

        quantum = 4;//random.nextInt(91) + 10;  // quantum entre 10 y 100
        numeroProcesos = random.nextInt(31); // N procesos entre 0 - 30
        
        SimuladorMLQ simulador = new SimuladorMLQ(cambioContexto, quantum, numeroProcesos);
        
        //Mostrar datos iniciales
        System.out.println("Quantum: " + quantum);
        System.out.println("CDC: " + cambioContexto);
        System.out.println();

    }
}