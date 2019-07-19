package reflection.api.sherlock.holmes;

import reflection.api.Investigator;

import java.lang.reflect.Method;
import java.util.Set;

public class SherlockHolmes implements Investigator {
    private Object Interrogee;

    @Override
    public void load(Object anInstanceOfSomething) {
        this.Interrogee = anInstanceOfSomething;
    }

    @Override
    public int getTotalNumberOfMethods() {
        Method[] methods = Interrogee.getClass().getMethods();
        return methods.length;
    }

    @Override
    public int getTotalNumberOfConstructors() {
        return 0;
    }

    @Override
    public int getTotalNumberOfFields() {
        return 0;
    }

    @Override
    public Set<String> getAllImplementedInterfaces() {
        return null;
    }

    @Override
    public int getCountOfConstantFields() {
        return 0;
    }

    @Override
    public int getCountOfStaticMethods() {
        return 0;
    }

    @Override
    public boolean isExtending() {
        return false;
    }

    @Override
    public String getParentClassSimpleName() {
        return null;
    }

    @Override
    public boolean isParentClassAbstract() {
        return false;
    }

    @Override
    public Set<String> getNamesOfAllFieldsIncludingInheritanceChain() {
        return null;
    }

    @Override
    public int invokeMethodThatReturnsInt(String methodName, Object... args) {
        return 0;
    }

    @Override
    public Object createInstance(int numberOfArgs, Object... args) {
        return null;
    }

    @Override
    public Object elevateMethodAndInvoke(String name, Class<?>[] parametersTypes, Object... args) {
        return null;
    }

    @Override
    public String getInheritanceChain(String delimiter) {
        return null;
    }
}
