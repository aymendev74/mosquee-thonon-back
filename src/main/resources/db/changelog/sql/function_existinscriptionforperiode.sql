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