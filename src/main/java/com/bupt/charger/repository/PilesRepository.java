package com.bupt.charger.repository;
import com.bupt.charger.entity.Pile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PilesRepository extends JpaRepository<Pile, Long>{
    Pile findByPileId(String pileId);

    default Pile findByPileId(int pile) {
        final String[] piles = {"CFA", "CFB", "CTA", "CTB", "CTC"};

        return findByPileId(piles[pile]);
    }

    default Pile findByPile(String pile) {
        if (Character.isDigit(pile.charAt(0))) {
            return findByPileId(Integer.parseInt(pile));
        } else {
            return findByPileId(pile);
        }
    }
}
