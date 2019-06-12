import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {
    private static Random rand = new Random();
    public static void main(String[] args) throws InterruptedException {
        int nProcesses = Integer.parseInt(args[0]);
        int nFaulty = Integer.parseInt(args[1]);
        long sleepTime = Long.parseLong(args[2]);
        final ActorSystem system = ActorSystem.create("system");
        final ArrayList<ActorRef> actors = new ArrayList<>();
        for(int x = 0; x <= nProcesses-1; x = x + 1) {
            actors.add(x,
                    system.actorOf(Process.createActor(x), "P"+ x));
        }
        for(int x = 0; x <= nProcesses-1; x = x + 1) {
            actors.get(x).tell(new Process.ActorListMessage(actors), ActorRef.noSender());
        }

        Collections.shuffle(actors);
        for(int i=0; i<nFaulty; i++){
            actors.get(i).tell(new Process.CrashMessage(), ActorRef.noSender());
        }

        for(int i=0; i<nProcesses; i++){
            actors.get(i).tell(new Process.LaunchMessage(), ActorRef.noSender());
        }

        Thread.sleep(sleepTime);

        int leader = rand.nextInt(nProcesses-nFaulty)+nFaulty;
        for(int i=0; i<nProcesses; i++){
            if(i != leader){
                actors.get(i).tell(new Process.HoldMessage(), ActorRef.noSender());
            }
        }

        Thread.sleep(1000);

        for(int i=0; i<nProcesses; i++){
            system.stop(actors.get(i));
        }

        System.exit(0);
    }
}
