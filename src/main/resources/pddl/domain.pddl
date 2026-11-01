(define (domain sokoban)
    (:requirements :typing)
    (:types location direction)

    (:predicates
        ;Pour exprimer que le joueur se trouve sur une case spécifique
        (player-at ?loc - location)

        ;Pour exprimer qu'une caisse se trouve sur une case spécifique
        (box-at ?loc - location)

        ;Pour exprimer qu'une case est libre
        (clear ?loc - location)

        ;Pour exprimer que la case à la localisation ?loc2 est en direction ?dir de la case à la localisation ?loc1
        (adjacent ?loc1 ?loc2 - location ?dir - direction)
    )

    (:action move
        :parameters (?from ?to - location ?dir - direction)
        :precondition (and
            (player-at ?from) ;Le joueur est sur la case ?from
            (adjacent ?from ?to ?dir) ;La case ?to est adjacente à ?from dans la direction ?dir
            (clear ?to) ;La case ?to est libre
        )
        :effect (and
            (player-at ?to) ;Le joueur est maintenant sur la case ?to
            (not (player-at ?from)) ;Le joueur n'est plus sur la case ?from
            (clear ?from) ;La case ?from est maintenant libre
            (not (clear ?to)) ;La case ?to n'est plus libre
        )
    )

    (:action push
        :parameters (?loc-joueur ?loc-caisse ?loc-destination - location ?dir - direction)
        :precondition(and
            (clear ?loc-destination)
            (player-at ?loc-joueur)
            (box-at ?loc-caisse)
            (adjacent ?loc-joueur ?loc-caisse ?dir)
            (adjacent ?loc-caisse ?loc-destination ?dir)
        )
        :effect (and
            (player-at ?loc-caisse)
            (not (player-at ?loc-joueur))
            (box-at ?loc-destination)
            (not (box-at ?loc-caisse))
            (not (clear ?loc-destination))
            (clear ?loc-joueur)
        )
    )
)