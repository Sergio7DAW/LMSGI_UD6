
for $baile in doc("DB_BailesDeSalon.xml")/Bailes/baile
where $baile/sala = "1"
return $baile
