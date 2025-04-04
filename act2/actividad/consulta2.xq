for $baile in doc("DB_BailesDeSalon.xml")/Bailes/baile
where $baile/sala = "2" and $baile/precio <= 35 and $baile/precio/@moneda = "euro"
return $baile

