CREATE TABLE moth.utilisateur (
	idutil bigserial NOT NULL,
	txutiluser varchar(50),
	txutilpassword varchar(255),
	oh_date_cre date,
	oh_vis_cre varchar(20),
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT utilisateur_pkey PRIMARY KEY (idutil)
);


create table moth.resplegal(
	idresp bigserial not null,
	txrespnom varchar(50) NOT NULL,
	txrespprenom varchar(50) NOT NULL,
	txrespphone varchar(20),
	txrespmobile varchar(20),
	txrespemail varchar(100) NOT NULL,
	txrespnumrue varchar(255) NOT NULL,
	norespcodepostal int4 NOT NULL,
	txrespville varchar(100) NOT NULL,
	oh_date_cre date NOT NULL,
	oh_vis_cre varchar(20) NOT NULL,
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT resp_pkey PRIMARY KEY (idresp)
);

CREATE TABLE moth.inscription (
	idinsc bigserial NOT NULL,
	idresp bigint not null,
	dtinscinscription date not null,
	oh_date_cre date NOT NULL,
	oh_vis_cre varchar(20) NOT NULL,
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT inscription_pkey PRIMARY KEY (idinsc),
	CONSTRAINT fk_idresp FOREIGN KEY(idresp) REFERENCES moth.resplegal(idresp)
);

create table moth.eleve(
	idelev bigserial NOT NULL,
	idinsc bigint not null,
	txelevnom varchar(50) NOT NULL,
	txelevprenom varchar(50) NOT NULL,
	dtelevnaissance date NOT NULL,
	cdelevniveau varchar(20),
	oh_date_cre date NOT NULL,
	oh_vis_cre varchar(20) NOT NULL,
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT eleve_pkey PRIMARY KEY (idelev),
	CONSTRAINT fk_idinsc FOREIGN KEY(idinsc) REFERENCES moth.inscription(idinsc)
);

CREATE TABLE moth.tarif (
	idtari bigserial NOT NULL,
	idperi bigint NOT NULL,
	cdtariapplication varchar(50) NOT NULL,
	cdtaritype varchar(50),
	lotariadherent bool,
	nbtarienfant int,
	mttari decimal NOT NULL,
	oh_date_cre date NOT NULL,
	oh_vis_cre varchar(20) NOT NULL,
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT tarif_pkey PRIMARY KEY (idtari),
	CONSTRAINT fk_idperi FOREIGN KEY(idperi) REFERENCES moth.periode(idperi)
);

CREATE TABLE moth.periode (
	idperi bigserial NOT NULL,
	dtperidebut date,
	dtperifin date,
	nbperimaxinscription int,
	oh_date_cre date NOT NULL,
	oh_vis_cre varchar(20) NOT NULL,
	oh_date_mod date,
	oh_vis_mod varchar(20),
	CONSTRAINT periode_pkey PRIMARY KEY (idperi)
);

insert into moth.periode (dtperidebut, dtperifin, nbperimaxinscription, oh_date_cre, oh_vis_cre)
values (to_date('01092023','DDMMYYYY'), to_date('31082024','DDMMYYYY'), 15, current_date, 'aymen');


-------------------- Tarifs adhésion -----------------------------------------------------

insert into moth.tarif (idperi, cdtariapplication, mttari, oh_date_cre, oh_vis_cre)
values (1, 'ADHESION', 15, current_date, 'aymen');
insert into moth.tarif (idperi, cdtariapplication, mttari, oh_date_cre, oh_vis_cre)
values (1, 'ADHESION', 20, current_date, 'aymen');
insert into moth.tarif (idperi, cdtariapplication, mttari, oh_date_cre, oh_vis_cre)
values (1, 'ADHESION', 30, current_date, 'aymen');

------------------- Tarifs BASE inscription cours arabes ----------------------------------

-- Adhérent, 1 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', true, 1, 120, current_date, 'aymen');
-- Non adhérent, 1 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', false, 1, 240, current_date, 'aymen');

-- Adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', true, 2, 160, current_date, 'aymen');
-- Non adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', false, 2, 280, current_date, 'aymen');

-- Adhérent, 3 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', true, 3, 200, current_date, 'aymen');
-- Non adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', false, 3, 320, current_date, 'aymen');

-- Adhérent, 4 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', true, 4, 240, current_date, 'aymen');
-- Non adhérent, 4 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'BASE', false, 4, 360, current_date, 'aymen');


-------------- Tarifs PAR ENFANT inscription cours arabes ---------------------------
-- Adhérent, 1 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', true, 1, 15, current_date, 'aymen');
-- Non adhérent, 1 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', false, 1, 15, current_date, 'aymen');

-- Adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', true, 2, 15, current_date, 'aymen');
-- Non adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', false, 2, 15, current_date, 'aymen');

-- Adhérent, 3 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', true, 3, 15, current_date, 'aymen');
-- Non adhérent, 2 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', false, 3, 15, current_date, 'aymen');

-- Adhérent, 4 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', true, 4, 15, current_date, 'aymen');
-- Non adhérent, 4 enfant
insert into moth.tarif (idperi, cdtariapplication, cdtaritype, lotariadherent, nbtarienfant, mttari, oh_date_cre, oh_vis_cre)
values (1, 'COURS', 'ENFANT', false, 4, 15, current_date, 'aymen');