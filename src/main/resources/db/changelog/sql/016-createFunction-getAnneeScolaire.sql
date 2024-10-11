drop function if exists moth.getAnneeScolaire;
CREATE FUNCTION moth.getAnneeScolaire(idInscription bigint)
 RETURNS varchar
 LANGUAGE plpgsql
AS $function$
declare
   anneeScolaire varchar;
begin
   select concat(peri.noperianneedebut, '/', peri.noperianneefin)   into anneeScolaire
   from moth.inscription insc
   inner join moth.resplegal resp on resp.idresp = insc.idresp 
   inner join moth.tarif tari on tari.idtari = resp.idtari
   inner join moth.periode peri on peri.idperi = tari.idperi
   where
   insc.idinsc = idInscription;

  return anneeScolaire;
 
end;
$function$