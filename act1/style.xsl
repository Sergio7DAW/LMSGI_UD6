<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <html>
            <head>
                <title>Lista de Libros</title>
                <style>
                    table { border-collapse: collapse; width: 100%; }
                    th, td { border: 1px solid black; padding: 8px; text-align: left; }
                    th { background-color: #f2f2f2; }
                </style>
            </head>
            <body>
                <h2>Biblioteca de Libros</h2>
                <table>
                    <tr>
                        <th>Código</th>
                        <th>Título</th>
                        <th>Editorial</th>
                        <th>Edición</th>
                        <th>ISBN</th>
                        <th>Páginas</th>
                        <th>Autor</th>
                        <th>Nacionalidad</th>
                    </tr>
                    <xsl:for-each select="LibrosBD/libro">
                        <tr>
                            <td><xsl:value-of select="Cod_Libro"/></td>
                            <td><xsl:value-of select="Titulo"/></td>
                            <td><xsl:value-of select="Editorial"/></td>
                            <td><xsl:value-of select="Edicion"/></td>
                            <td><xsl:value-of select="ISBN"/></td>
                            <td><xsl:value-of select="NumPaginas"/></td>
                            <td><xsl:value-of select="Autores/autor/Nombre"/> <xsl:value-of select="Autores/autor/Apellidos"/></td>
                            <td><xsl:value-of select="Autores/autor/Nacionalidad"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
