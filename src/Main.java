import java.util.Random;

public class Main {
    public static void main(String[] args) {

        Random random = new Random();
        
        final int cambioContexto = 1; //Establecer el cambio de contexto
        final int numeroColas = 2; //Establecer el numero de colas
        final int quantum;
        final int numeroProcesos;

        quantum = random.nextInt(91) + 10;  // quantum entre 10 y 100
        numeroProcesos = random.nextInt(31); // N procesos entre 0 - 30
        
        //Crear la instancia para el algoritmo
        AdministradorMLQ administrador = new AdministradorMLQ(cambioContexto, quantum, numeroProcesos);

        administrador.crearColas(numeroColas);
        administrador.crearProcesosEnColas();

        //Imprimir data
        for(Cola cola: administrador.getColas()){
            cola.getProcesos().forEach(
                proceso -> System.out.println(proceso.toString())
            );
        }

    }
}