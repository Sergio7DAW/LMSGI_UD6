
distinct-values(
    for $baile in doc("DB_BailesDeSalon.xml")/Bailes/baile
    where $baile/precio/@cuota = "mensual"
    return $baile/profesor
)
