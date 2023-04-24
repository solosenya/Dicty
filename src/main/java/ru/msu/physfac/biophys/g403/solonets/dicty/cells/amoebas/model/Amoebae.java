package ru.msu.physfac.biophys.g403.solonets.dicty.cells.amoebas.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "amoebas")
public class Amoebae {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "position")
    private Integer position;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "cell_id")
    private Integer cellId;

    @Column(name = "time")
    private Integer time;

    @Column(name = "destination")
    private Destination destination;

    public enum State {
        EXCITED,
        RESTING,
        READY,
        EMPTY,
        PACESETTER
    }

    public enum Destination {
        RIGHT(1, 0),
        LEFT(-1, 0),
        TOP(0, 1),
        BOTTOM(0, -1),
        LEFT_BOTTOM(-1, -1),
        RIGHT_BOTTOM(1, -1),
        LEFT_TOP(-1, 1),
        RIGHT_TOP(1, 1);

        @Getter
        private final int difX;
        @Getter
        private final int difY;

        Destination(int difX, int difY) {
            this.difX = difX;
            this.difY = difY;
        }

        public static Set<Destination> selectNeighbouringDests(Destination dest) {
            return switch (dest) {
                case RIGHT -> Set.of(RIGHT_TOP, RIGHT_BOTTOM, dest);
                case LEFT -> Set.of(LEFT_BOTTOM, LEFT_TOP, dest);
                case TOP -> Set.of(LEFT_TOP, RIGHT_TOP, dest);
                case BOTTOM -> Set.of(LEFT_BOTTOM, RIGHT_BOTTOM, dest);
                case LEFT_BOTTOM -> Set.of(LEFT, BOTTOM, dest);
                case RIGHT_BOTTOM -> Set.of(RIGHT, BOTTOM, dest);
                case LEFT_TOP -> Set.of(LEFT, TOP, dest);
                case RIGHT_TOP -> Set.of(RIGHT, TOP, dest);
            };
        }
    }
}
