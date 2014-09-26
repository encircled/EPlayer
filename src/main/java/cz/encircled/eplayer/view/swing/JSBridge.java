package cz.encircled.eplayer.view.swing;

/**
 * Created by Encircled on 15/09/2014.
 */
public class JSBridge {

    public void myMethod(String e) {
        System.out.println("MY METHOD: " + e);

//        System.out.println(FX.windowObject.call("tetete", new Object[]{"JAVA!!!"}));
    }

}
