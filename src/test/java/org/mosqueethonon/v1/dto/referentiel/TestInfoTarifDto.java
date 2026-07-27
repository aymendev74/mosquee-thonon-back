package org.mosqueethonon.v1.dto.referentiel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.annotations.CodeTarifEnfant;
import org.mosqueethonon.annotations.TarifAdulte;
import org.mosqueethonon.enums.TypeTarifEnum;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Ces tests verrouillent le contenu des annotations de {@link InfoTarifDto}.
 *
 * <p>Ces annotations ne sont pas de la simple documentation : {@code TarifAdminServiceImpl}
 * les lit par réflexion pour créer les {@code TarifEntity} d'une nouvelle période, et
 * {@code TarifCalculServiceImpl} recherche ensuite les tarifs par le triplet
 * (type, adherent, nbEnfant). Une valeur erronée dans une annotation produit donc en base
 * un tarif introuvable au calcul — et l'inscription concernée devient impossible.
 *
 * <p>C'est exactement ce qui s'est produit sur {@code ENFANT_4_ENFANT}, annoté
 * {@code type = BASE} au lieu de {@code type = ENFANT} : aucun tarif de type ENFANT
 * n'existait pour 4 enfants non adhérents, et le calcul renvoyait null.
 */
public class TestInfoTarifDto {

    /** BASE_ADHERENT_2_ENFANT -> type=BASE, adherent=true, nbEnfant=2 */
    private static final Pattern CODE_TARIF = Pattern.compile("^(BASE|ENFANT)_(ADHERENT_)?(\\d+)_ENFANT$");

    private static final int NB_ENFANT_MIN = 1;
    private static final int NB_ENFANT_MAX = 4;

    private List<Field> champsTarifEnfant() {
        return Arrays.stream(InfoTarifDto.class.getDeclaredFields())
                .filter(field -> field.getAnnotation(CodeTarifEnfant.class) != null)
                .collect(Collectors.toList());
    }

    private CodeTarifEnfant annotationDuCode(String codeTarif) {
        return champsTarifEnfant().stream()
                .map(field -> field.getAnnotation(CodeTarifEnfant.class))
                .filter(annotation -> codeTarif.equals(annotation.codeTarif()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucun champ annoté avec le code tarif " + codeTarif));
    }

    private List<Field> champsTarifAdulte() {
        return Arrays.stream(InfoTarifDto.class.getDeclaredFields())
                .filter(field -> field.getAnnotation(TarifAdulte.class) != null)
                .collect(Collectors.toList());
    }

    @Test
    public void testChaqueAnnotationEstCoherenteAvecSonCodeTarif() {
        List<String> erreurs = new ArrayList<>();

        for (Field field : champsTarifEnfant()) {
            CodeTarifEnfant annotation = field.getAnnotation(CodeTarifEnfant.class);
            Matcher matcher = CODE_TARIF.matcher(annotation.codeTarif());
            if (!matcher.matches()) {
                erreurs.add(field.getName() + " : codeTarif '" + annotation.codeTarif() + "' ne respecte pas le format attendu");
                continue;
            }

            TypeTarifEnum typeAttendu = TypeTarifEnum.valueOf(matcher.group(1));
            boolean adherentAttendu = matcher.group(2) != null;
            int nbEnfantAttendu = Integer.parseInt(matcher.group(3));

            if (annotation.type() != typeAttendu) {
                erreurs.add(annotation.codeTarif() + " : type=" + annotation.type() + " au lieu de " + typeAttendu);
            }
            if (annotation.adherent() != adherentAttendu) {
                erreurs.add(annotation.codeTarif() + " : adherent=" + annotation.adherent() + " au lieu de " + adherentAttendu);
            }
            if (annotation.nbEnfant() != nbEnfantAttendu) {
                erreurs.add(annotation.codeTarif() + " : nbEnfant=" + annotation.nbEnfant() + " au lieu de " + nbEnfantAttendu);
            }
        }

        assertTrue(erreurs.isEmpty(), "Annotations incohérentes avec leur codeTarif :\n" + String.join("\n", erreurs));
    }

    @Test
    public void testLeTarifParEnfantPour4EnfantsEstBienDeTypeEnfant() {
        // Régression historique : ce champ était annoté type=BASE, rendant impossible
        // le calcul du tarif d'une inscription à 4 enfants non adhérents.
        CodeTarifEnfant annotation = annotationDuCode("ENFANT_4_ENFANT");

        assertEquals(TypeTarifEnum.ENFANT, annotation.type());
        assertEquals(4, annotation.nbEnfant());
        assertFalse(annotation.adherent());
    }

    @Test
    public void testLesQuatreTarifsDe4EnfantsSontCorrectementTypes() {
        assertEquals(TypeTarifEnum.BASE, annotationDuCode("BASE_4_ENFANT").type());
        assertEquals(TypeTarifEnum.BASE, annotationDuCode("BASE_ADHERENT_4_ENFANT").type());
        assertEquals(TypeTarifEnum.ENFANT, annotationDuCode("ENFANT_4_ENFANT").type());
        assertEquals(TypeTarifEnum.ENFANT, annotationDuCode("ENFANT_ADHERENT_4_ENFANT").type());
    }

    @Test
    public void testToutesLesCombinaisonsDeTarifEnfantSontPresentesUneSeuleFois() {
        // Le calcul recherche un tarif par (type, adherent, nbEnfant) : chaque combinaison
        // doit exister, et une seule fois, sinon le tarif retenu devient arbitraire.
        Set<String> combinaisons = new HashSet<>();
        for (Field field : champsTarifEnfant()) {
            CodeTarifEnfant annotation = field.getAnnotation(CodeTarifEnfant.class);
            String combinaison = annotation.type() + "|" + annotation.adherent() + "|" + annotation.nbEnfant();
            assertTrue(combinaisons.add(combinaison),
                    "Combinaison (type, adherent, nbEnfant) en double : " + combinaison
                            + " sur " + annotation.codeTarif());
        }

        List<String> manquantes = new ArrayList<>();
        for (TypeTarifEnum type : List.of(TypeTarifEnum.BASE, TypeTarifEnum.ENFANT)) {
            for (boolean adherent : List.of(false, true)) {
                for (int nbEnfant = NB_ENFANT_MIN; nbEnfant <= NB_ENFANT_MAX; nbEnfant++) {
                    String combinaison = type + "|" + adherent + "|" + nbEnfant;
                    if (!combinaisons.contains(combinaison)) {
                        manquantes.add(combinaison);
                    }
                }
            }
        }

        assertTrue(manquantes.isEmpty(), "Combinaisons de tarif enfant manquantes : " + manquantes);
        assertEquals(16, combinaisons.size());
    }

    @Test
    public void testLesCodesTarifSontUniques() {
        Set<String> codes = new HashSet<>();
        for (Field field : champsTarifEnfant()) {
            String code = field.getAnnotation(CodeTarifEnfant.class).codeTarif();
            assertTrue(codes.add(code), "Code tarif en double : " + code);
        }
        assertEquals(16, codes.size());
    }

    @Test
    public void testChaqueTypeDeTarifAdulteEstPresentUneSeuleFois() {
        Set<TypeTarifEnum> types = new HashSet<>();
        for (Field field : champsTarifAdulte()) {
            TypeTarifEnum type = field.getAnnotation(TarifAdulte.class).type();
            assertTrue(types.add(type), "Type de tarif adulte en double : " + type);
        }

        assertEquals(Set.of(TypeTarifEnum.ETUDIANT, TypeTarifEnum.AVEC_ACTIVITE, TypeTarifEnum.SANS_ACTIVITE), types);
    }

    @Test
    public void testTousLesMontantsPortentUneAnnotationDeTarif() {
        // Un champ montant sans annotation serait silencieusement ignoré à la création
        // comme à la lecture des tarifs.
        List<String> sansAnnotation = Arrays.stream(InfoTarifDto.class.getDeclaredFields())
                .filter(field -> field.getType() == BigDecimal.class)
                .filter(field -> field.getAnnotation(CodeTarifEnfant.class) == null
                        && field.getAnnotation(TarifAdulte.class) == null)
                .map(Field::getName)
                .collect(Collectors.toList());

        assertTrue(sansAnnotation.isEmpty(), "Champs montant sans annotation de tarif : " + sansAnnotation);
    }
}
