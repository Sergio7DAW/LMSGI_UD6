
<html>
    <body>
        <table border="1">
            <tr>
                <th>Baile</th>
                <th>Profesor</th>
                <th>Plazas</th>
            </tr>
            {
                for $baile in doc("DB_BailesDeSalon.xml")/Bailes/baile
                where $baile/precio/@cuota = "trimestral"
                order by $baile/plazas ascending
                return
                <tr>
                    <td>{ $baile/nombre/text() }</td>
                    <td>{ $baile/profesor/text() }</td>
                    <td>{ $baile/plazas/text() }</td>
                </tr>
            }
        </table>
    </body>
</html>

