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
	idtari int8 NOT NULL,
    txrespnom varchar(50) NOT NULL,
    txrespprenom varchar(50) NOT NULL,
    txrespphone varchar(20),
    txrespmobile varchar(20),
    txrespemail varchar(100) NOT NULL,
    txrespnumrue varchar(255) NOT NULL,
    norespcodepostal int4 NOT NULL,
    txrespville varchar(100) NOT NULL,
    lorespadherent bool NOT NULL,
    lorespautonomie bool not null,
    lorespmedia bool not null,
    txrespnomautre varchar(50),
    txrespprenomautre varchar(50),
    txresplienparente varchar(50),
    oh_date_cre date NOT NULL,
    oh_vis_cre varchar(20) NOT NULL,
    oh_date_mod date,
    oh_vis_mod varchar(20),
	CONSTRAINT resp_pkey PRIMARY KEY (idresp)
);

CREATE TABLE moth.inscription (
	idinsc bigserial NOT NULL,
	idresp int8 NOT NULL,
    dtinscinscription date NOT NULL,
    cdinscstatut varchar(20) NOT NULL,
    oh_date_cre date NOT NULL,
    oh_vis_cre varchar(20) NOT NULL,
    oh_date_mod date,
    oh_vis_mod varchar(20),
	CONSTRAINT inscription_pkey PRIMARY KEY (idinsc),
	CONSTRAINT fk_idresp FOREIGN KEY(idresp) REFERENCES moth.resplegal(idresp)
);

create table moth.eleve(
	idelev bigserial NOT NULL,
	idinsc int8 NOT NULL,
    idtari int8 NOT NULL,
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

create table moth.adhesion(
	idadhe bigserial NOT NULL,
    idtari int8 NOT NULL,
    cdadhestatut varchar(20) NOT NULL,
    cdadhetitre varchar(10) NOT null,
    txadhenom varchar(50) NOT NULL,
    txadheprenom varchar(50) NOT NULL,
    dtadhenaissance date NOT NULL,
    txadhephone varchar(20),
    txadhemobile varchar(20),
    txadheemail varchar(100) NOT NULL,
    txadhenumrue varchar(255) NOT NULL,
    noadhecodepostal int4 NOT NULL,
    txadheville varchar(100) NOT NULL,
    mtadheautre int4,
    oh_date_cre date NOT NULL,
    oh_vis_cre varchar(20) NOT NULL,
    oh_date_mod date,
    oh_vis_mod varchar(20),
	CONSTRAINT adhesion_pkey PRIMARY KEY (idadhe),
	CONSTRAINT fk_idtari FOREIGN KEY(idtari) REFERENCES moth.tarif(idtari)
);

create or replace view moth.v_inscription_light as
SELECT e.idelev AS id,
    i.idinsc AS idinscription,
    i.dtinscinscription AS dateinscription,
    i.cdinscstatut AS statut,
    e.txelevnom AS nom,
    e.txelevprenom AS prenom,
    e.dtelevnaissance AS datenaissance,
    e.cdelevniveau AS niveau,
    r.txrespphone AS telephone,
    r.txrespmobile AS mobile,
    r.txrespville AS ville
   FROM ((moth.inscription i
     JOIN moth.eleve e ON ((e.idinsc = i.idinsc)))
     JOIN moth.resplegal r ON ((i.idresp = r.idresp)));

create or replace view moth.v_adhesion_light as
select a.idadhe as id, a.txadhenom as nom, a.txadheprenom as prenom, a.txadheville as ville,
a.cdadhestatut as statut, coalesce(a.mtadheautre, t.mttari) as montant,
a.dtadheinscription as dateInscription
from moth.adhesion a
inner join moth.tarif t on t.idtari = a.idtari
;

CREATE TABLE moth.tarif (
	idtari bigserial NOT NULL,
	idperi bigint NOT NULL,
	cdtariapplication varchar(50) NOT NULL,
	cdtaritype varchar(50),
	lotariadherent bool,
	nbtarienfant int,
	mttari decimal,
	cdtaritarif varchar(50),
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

create or replace view moth.v_periode_cours as
select distinct peri.idperi as id, peri.dtperidebut as datedebut, peri.dtperifin as datefin,
peri.nbperimaxinscription as nbmaxinscription,
moth.existInscriptionForPeriode(peri.idperi) as existInscription, peri.oh_date_cre,
peri.oh_vis_cre, peri.oh_date_mod, peri.oh_vis_mod,
	case
		when current_date between peri.dtperidebut and peri.dtperifin then true
		else false
	end as active
from moth.periode peri
;

create or replace function moth.existInscriptionForPeriode(idPeriode int8)
returns boolean
language plpgsql
as
$$
declare
   idElev int8;
begin
   select elev.idElev
   into idElev
   from moth.eleve elev
   inner join moth.tarif tari on tari.idtari = elev.idtari
   inner join moth.periode peri on peri.idperi = tari.idperi
   where
   peri.idPeri = idPeriode
   limit 1;

  IF idElev > 0 THEN
   return true;
  else return false;
 end if;
end;
$$;

insert into moth.periode (dtperidebut, dtperifin, nbperimaxinscription, oh_date_cre, oh_vis_cre)
values (to_date('01092023','DDMMYYYY'), to_date('31082024','DDMMYYYY'), 15, current_date, 'aymen');

------------------- Tarifs adhésions et cours arabes ----------------------------------

INSERT INTO moth.tarif (idperi,cdtariapplication,cdtaritype,lotariadherent,nbtarienfant,mttari,oh_date_cre,oh_vis_cre,oh_date_mod,oh_vis_mod,cdtaritarif) VALUES
	 (1,'ADHESION','FIXE',NULL,NULL,15,'2023-12-29','aymen',NULL,NULL,NULL),
	 (1,'ADHESION','FIXE',NULL,NULL,20,'2023-12-29','aymen',NULL,NULL,NULL),
	 (1,'ADHESION','FIXE',NULL,NULL,30,'2023-12-29','aymen',NULL,NULL,NULL),
	 (1,'ADHESION','LIBRE',NULL,NULL,NULL,'2023-12-29','aymen',NULL,NULL,NULL),
	 (1,'COURS','BASE',true,1,120,'2023-11-20','aymen',NULL,NULL,'BASE_ADHERENT_1_ENFANT'),
	 (1,'COURS','BASE',false,1,240,'2023-11-20','aymen',NULL,NULL,'BASE_1_ENFANT'),
	 (1,'COURS','BASE',true,2,160,'2023-11-20','aymen',NULL,NULL,'BASE_ADHERENT_2_ENFANT'),
	 (1,'COURS','BASE',false,2,280,'2023-11-20','aymen',NULL,NULL,'BASE_2_ENFANT'),
	 (1,'COURS','BASE',true,3,200,'2023-11-20','aymen',NULL,NULL,'BASE_ADHERENT_3_ENFANT'),
	 (1,'COURS','BASE',false,3,320,'2023-11-20','aymen',NULL,NULL,'BASE_3_ENFANT');
INSERT INTO moth.tarif (idperi,cdtariapplication,cdtaritype,lotariadherent,nbtarienfant,mttari,oh_date_cre,oh_vis_cre,oh_date_mod,oh_vis_mod,cdtaritarif) VALUES
	 (1,'COURS','BASE',true,4,240,'2023-11-20','aymen',NULL,NULL,'BASE_ADHERENT_4_ENFANT'),
	 (1,'COURS','BASE',false,4,360,'2023-11-20','aymen',NULL,NULL,'BASE_4_ENFANT'),
	 (1,'COURS','ENFANT',true,1,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_ADHERENT_1_ENFANT'),
	 (1,'COURS','ENFANT',false,1,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_1_ENFANT'),
	 (1,'COURS','ENFANT',true,2,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_ADHERENT_2_ENFANT'),
	 (1,'COURS','ENFANT',false,2,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_2_ENFANT'),
	 (1,'COURS','ENFANT',true,3,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_ADHERENT_3_ENFANT'),
	 (1,'COURS','ENFANT',false,3,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_3_ENFANT'),
	 (1,'COURS','ENFANT',true,4,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_ADHERENT_4_ENFANT'),
	 (1,'COURS','ENFANT',false,4,15,'2023-11-20','aymen',NULL,NULL,'ENFANT_4_ENFANT');
