drop table if exists moth.v_adhesion_light;

create or replace view moth.v_adhesion_light as
select a.idadhe as id, a.txadhenom as nom, a.txadheprenom as prenom, a.txadheville as ville,
a.cdadhestatut as statut, coalesce(a.mtadheautre, t.mttari) as montant,
a.dtadheinscription as dateInscription
from moth.adhesion a
inner join moth.tarif t on t.idtari = a.idtari
;