import { useState } from 'react'
import './App.css'
import { Placeholder } from './components/Placeholder'
import { SchemaList } from './components/SchemaList'
import { StatusBar } from './components/StatusBar'
import { TableList } from './components/TableList'
import { TablePanel } from './components/TablePanel'

function App() {
  const [selectedSchema, setSelectedSchema] = useState<string | null>(null)
  const [selectedTable, setSelectedTable] = useState<string | null>(null)

  function selectSchema(schema: string) {
    setSelectedSchema(schema)
    setSelectedTable(null)
  }

  return (
    <div className="app">
      <StatusBar />
      <div className="explorer">
        <div className="explorer__schemas">
          <SchemaList selected={selectedSchema} onSelect={selectSchema} />
        </div>
        <div className="explorer__tables">
          {selectedSchema ? (
            <TableList schema={selectedSchema} selected={selectedTable} onSelect={setSelectedTable} />
          ) : (
            <Placeholder text="Select a schema" />
          )}
        </div>
        <div className="explorer__columns">
          {selectedSchema && selectedTable ? (
            <TablePanel schema={selectedSchema} table={selectedTable} />
          ) : (
            <Placeholder text="Select a table" />
          )}
        </div>
      </div>
    </div>
  )
}

export default App
