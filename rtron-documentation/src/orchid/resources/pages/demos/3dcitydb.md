---
title: 3D City Database
---

# 3D City Database

The [3D City Database](https://www.3dcitydb.org/3dcitydb/) is a free 3D geo database to store, represent, and manage virtual 3D city models.
It is used by cities like Singapore, Berlin, Helsinki and many others.

The database schema implements the CityGML standard facilitating complex analysis tasks, far beyond visualization.
It can be deployed on top of the relational database [PostgreSQL](https://www.postgresql.org) with the GIS extension [PostGIS](https://postgis.net) installed.

## Deployment

Start the 3D City Database with a [docker container](https://github.com/tum-gis/3dcitydb-docker-postgis) by running this command:
```bash
docker run -dit --name citydb-container -p 5432:5432 \
    -e "POSTGRES_USER=postgres" \
    -e "POSTGRES_PASSWORD=changeit" \
    -e "CITYDBNAME=citydb" \
    -e "SRID=32632" \
    -e "SRSNAME=urn:ogc:def:crs:EPSG::32632" \
tumgis/3dcitydb-postgis
```

For a manual database setup checkout the [documentation](https://3dcitydb-docs.readthedocs.io/en/release-v4.2.3/intro/setup-3dcitydb.html#) of the 3D City Database.

## Loading Datasets

Load and extract CityGML datasets into the database with the [Importer/Exporter tool](https://github.com/3dcitydb/importer-exporter).
Therefore, setup the connection in the database tab and load the generated CityGML datasets into the database using the import tab:
![3DCityDB Query Answer](/assets/media/demos/3dcitydb-importerexporter-connect.png)

## Geospatial Analytics

Perform geospatial analysis on multiple transformed OpenDRIVE datasets.
For example, list all priority road signs ``(roadSignal_type = '306')`` sorted by distance to the ``POINT(678195.44 54 414.94)'::geometry)`` across multiple OpenDRIVE datasets:
```postgresql
SELECT
      cog0.strval as identifier_sourceFileName,
      cog1.intval as identifier_roadObjectId,
      cog2.strval as identifier_roadObjectName,
      cog3.strval as opendrive_roadSignal_type,
      ST_Distance(city_furniture.lod1_other_geom, 'SRID=32632;POINT(678195.4482485768 5403954.957612606 414.94568122784835)'::geometry) as distance
FROM
     city_furniture
INNER JOIN cityobject_genericattrib cog0 ON cog0.cityobject_id = city_furniture.id AND cog0.attrname = 'identifier_sourceFileName'
INNER JOIN cityobject_genericattrib cog1 ON cog1.cityobject_id = city_furniture.id AND cog1.attrname = 'identifier_roadObjectId'
INNER JOIN cityobject_genericattrib cog2 ON cog2.cityobject_id = city_furniture.id AND cog2.attrname = 'identifier_roadObjectName'
INNER JOIN cityobject_genericattrib cog3 ON cog3.cityobject_id = city_furniture.id AND cog3.attrname = 'opendrive_roadSignal_type'
WHERE cog3.strval = '306'
ORDER BY
  city_furniture.lod1_other_geom <#>
  'SRID=32632;POINT(678195.4482485768 5403954.957612606 414.94568122784835)'::geometry
```

This is the answer of the 3D City Database:
![3DCityDB Query Answer](/assets/media/demos/3dcitydb-query-answer.png)

## Extracting Datasets
Using the [Importer/Exporter tool](https://github.com/3dcitydb/importer-exporter), the 3D city model can be exported as CityGML and KML/COLLADA/glTF datasets:
![3DCityDB Query Answer](/assets/media/demos/3dcitydb-importerexporter-export.png)

## References

- [GitHub repository](https://github.com/3dcitydb/3dcitydb) of the 3D City Database
- [Documentation](https://3dcitydb-docs.readthedocs.io) of the 3D City Database
- Yao, Zhihang; Nagel, Claus; Kunde, Felix; Hudra, György; Willkomm, Philipp; Donaubauer, Andreas; Adolphi, Thomas; Kolbe, Thomas H.: [3DCityDB - a 3D geodatabase solution for the management, analysis, and visualization of semantic 3D city models based on CityGML](https://doi.org/10.1186/s40965-018-0046-7). Open Geospatial Data, Software and Standards 3 (5), 2018, 1-26.
