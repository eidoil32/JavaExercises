package reflection.api.sherlock.holmes;

import reflection.api.Investigator;

import java.lang.reflect.*;
import java.util.*;

public class SherlockHolmes implements Investigator {
    private Object Interrogate;
    private Class<?> InterrogateClass, superClass;

    @Override
    public void load(Object anInstanceOfSomething) {
        this.Interrogate = anInstanceOfSomething;
        InterrogateClass = Interrogate.getClass();
        superClass = InterrogateClass.getSuperclass();
    }

    @Override
    public int getTotalNumberOfMethods() {
        return InterrogateClass.getDeclaredMethods().length;
    }

    @Override
    public int getTotalNumberOfConstructors() {
        return InterrogateClass.getConstructors().length;
    }

    @Override
    public int getTotalNumberOfFields() {
        return InterrogateClass.getDeclaredFields().length;
    }

    @Override
    public Set<String> getAllImplementedInterfaces() {
        Set<String> interfaces = new HashSet<>();

        for (Class implementedInterface : InterrogateClass.getInterfaces()) {
            interfaces.add(implementedInterface.getSimpleName());
        }

        return interfaces;
    }

    @Override
    public int getCountOfConstantFields() {
        int counter = 0;

        for (Field field : InterrogateClass.getDeclaredFields()) {
            if (field.toString().contains("final")) {
                counter++;
            }
        }

        return counter;
    }

    @Override
    public int getCountOfStaticMethods() {
        Method[] methods = InterrogateClass.getMethods();
        int counter = 0;

        for (Method method : methods) {
            if (method.toString().contains("static")) {
                counter++;
            }
        }

        return counter;
    }

    @Override
    public boolean isExtending() {
        return !superClass.getSimpleName().equals("Object");
    }

    @Override
    public String getParentClassSimpleName() {
        return isExtending() ? superClass.getSimpleName() : null;
    }

    @Override
    public boolean isParentClassAbstract() {
        return superClass.getModifiers() == Modifier.ABSTRACT;
    }

    @Override
    public Set<String> getNamesOfAllFieldsIncludingInheritanceChain() {
        Set<String> names = new HashSet<>();
        Class klass = InterrogateClass;

        while (klass != null) {
            names.addAll(getNameOfFieldsByClass(klass));
            klass = klass.getSuperclass();
        }

        return names;
    }

    private Set<String> getNameOfFieldsByClass(Class klass) {
        Set<String> names = new HashSet<>();

        for (Field field : klass.getDeclaredFields()) {
            names.add(field.getName());
        }

        return names;
    }

    @Override
    public int invokeMethodThatReturnsInt(String methodName, Object... args) {
        try {
            Method methodToInvoke = InterrogateClass.getMethod(methodName);
            return (int) methodToInvoke.invoke(Interrogate, args);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return -1;
        }
    }

    @Override
    public Object createInstance(int numberOfArgs, Object... args) {
        for (Constructor ctor : InterrogateClass.getConstructors()) {
            if (ctor.getParameterCount() == numberOfArgs) {
                try {
                    return ctor.newInstance(args);
                } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) { return null; }
            }
        }
        return null;
    }

    @Override
    public Object elevateMethodAndInvoke(String name, Class<?>[] parametersTypes, Object... args) {
        try
        {
            Method method = InterrogateClass.getDeclaredMethod(name,parametersTypes);
            method.setAccessible(true);
            return method.invoke(Interrogate,args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    @Override
    public String getInheritanceChain(String delimiter) {
        return InheritanceNameRec(InterrogateClass, delimiter);
    }

    private String InheritanceNameRec(Class clazz, String delimiter) {
        if (clazz.getSuperclass() == null) {
            return clazz.getSimpleName();
        }

        return String.format("%s%s%s", InheritanceNameRec(clazz.getSuperclass(), delimiter), delimiter, clazz.getSimpleName());
    }
}