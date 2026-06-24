package targetcompare;

public class Mirna {

    private final String nome;
    private boolean alvo;

    public Mirna(String nome) {
        this.nome = nome;
        this.alvo = false;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAlvo() {
        return alvo;
    }

    public void setAlvo(boolean alvo) {
        this.alvo = alvo;
    }
}
