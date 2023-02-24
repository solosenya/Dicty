DROP TABLE IF EXISTS postgres.public.cells;

DROP TABLE IF EXISTS postgres.public.amoebas;

CREATE TABLE IF NOT EXISTS postgres.public.cells
(
    id BIGSERIAL PRIMARY KEY,
    x INTEGER NOT NULL,
    y INTEGER NOT NULL,
    cAMP_level INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS postgres.public.amoebas
(
    id BIGSERIAL PRIMARY KEY,
    position INTEGER NOT NULL,
    state VARCHAR(100) NOT NULL,
    cell_id INTEGER NOT NULL,
    time INTEGER NOT NULL
);