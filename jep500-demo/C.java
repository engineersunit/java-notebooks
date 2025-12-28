class C {
    final int x;

    C() {
        x = 100;
    }

    public static void main(String[] args) throws Exception {
        C c = new C();
        IO.println("Before: " + c.x);

        // Deep reflection to mutate a final field
        var f = C.class.getDeclaredField("x");
        f.setAccessible(true);

        // Mutations that will trigger JEP 500 behavior in JDK 26+
        f.set(c, 200);
        IO.println("After set to 200: " + c.x);

        f.set(c, 300);
        IO.println("After set to 300: " + c.x);

        IO.println("Done.");
    }
}
