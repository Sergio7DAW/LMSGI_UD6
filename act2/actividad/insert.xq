
declare updating function local:insertBaile() {
    insert node 
    <baile id="7">
        <nombre>Foxtrot</nombre>
        <precio cuota="mensual" moneda="dolares">22</precio>
        <plazas>12</plazas>
        <comienzo>01/01/2012</comienzo>
        <fin>31/07/2012</fin>
        <profesor>Freddy Astaire</profesor>
        <sala>3</sala>
    </baile>
    into doc("DB_BailesDeSalon.xml")/Bailes
};

local:insertBaile()

