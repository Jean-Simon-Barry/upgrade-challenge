-- so I had based this logic on a neat article https://info.crunchydata.com/blog/range-types-recursion-how-to-search-availability-with-postgresql
-- HOWEVER, there is a fatal flaw in the logic of this function where, if you request availability for dates which correspond _exactly_
-- with an existing reservation, it would return you that range as available. God bless integration tests!
CREATE OR REPLACE FUNCTION get_available_periods(daterange)
    RETURNS TABLE
            (
                available_dates daterange
            )
AS
$$
WITH RECURSIVE calendar AS (
    SELECT $1 AS left,
           $1 AS center,
           $1 AS right
    UNION
    SELECT CASE camping_reservation.reservation_dates && calendar.left
               WHEN TRUE THEN daterange(lower(calendar.left), lower(
                       camping_reservation.reservation_dates * calendar.left))
               ELSE daterange(lower(calendar.right),
                              lower(camping_reservation.reservation_dates * calendar.right))
               END AS left,
           CASE camping_reservation.reservation_dates && calendar.left
               WHEN TRUE THEN camping_reservation.reservation_dates * calendar.left
               ELSE camping_reservation.reservation_dates * calendar.right
               END AS center,
           CASE camping_reservation.reservation_dates && calendar.right
               WHEN TRUE THEN daterange(
                       upper(camping_reservation.reservation_dates * calendar.right),
                       upper(calendar.right))
               ELSE daterange(upper(camping_reservation.reservation_dates * calendar.left),
                              upper(calendar.left))
               END AS right
    FROM calendar
             JOIN camping_reservation ON
            camping_reservation.reservation_dates && $1 AND
            camping_reservation.reservation_dates <> calendar.center AND (
                    camping_reservation.reservation_dates && calendar.left OR
                    camping_reservation.reservation_dates && calendar.right
                )
)
SELECT *
FROM (
         SELECT a.left AS available_dates
         FROM calendar a
                  LEFT OUTER JOIN calendar b ON
                 a.left <> b.left AND
                 a.left @> b.left
         GROUP BY a.left
         HAVING NOT bool_or(COALESCE(a.left @> b.left, FALSE))
         UNION
         SELECT a.right AS available_dates
         FROM calendar a
                  LEFT OUTER JOIN calendar b ON
                 a.right <> b.right AND
                 a.right @> b.right
         GROUP BY a.right
         HAVING NOT bool_or(COALESCE(a.right @> b.right, FALSE))
         order by available_dates asc
     ) a
$$ LANGUAGE SQL;
