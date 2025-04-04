declare updating function local:updateBaile() {
    replace value of node doc("DB_BailesDeSalon.xml")/Bailes/baile[@id="7"]/profesor
    with "Angel Correllada";

    replace value of node doc("DB_BailesDeSalon.xml")/Bailes/baile[@id="7"]/plazas
    with "14";
};

local:updateBaile()