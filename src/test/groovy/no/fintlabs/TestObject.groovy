package no.fintlabs


class TestObject implements Serializable {
    String name
    int id

    TestObject(String name) {
        this.name = name
    }

    TestObject(String name, int id) {
        this.name = name
        this.id = id
    }

    public String getName() {
        return name
    }

    public int getId(){
        return id
    }

    @Override
    boolean equals(Object o) {
        if (!(o instanceof TestObject)) return false
        TestObject other = (TestObject) o
        return this.name.equals(other.name)
    }
}
