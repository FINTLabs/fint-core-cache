package no.fintlabs


class TestObject implements Serializable {
    String name

    TestObject(String name) {
        this.name = name
    }

    private String getName() {
        return name;
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof TestObject)) return false;
        TestObject other = (TestObject) o;
        return this.name.equals(other.name)
    }
}
