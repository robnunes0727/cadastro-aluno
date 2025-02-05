package br.com.exemplo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import br.com.exemplo.util.ConnectionFactory;
import br.com.exemplo.model.Aluno;
import br.com.exemplo.model.AlunoEmTurma;
import br.com.exemplo.model.Curso;
import br.com.exemplo.model.Turma;

public class AlunoEmTurmaDAO {
		
	// SQL stuff
	private PreparedStatement ps; 
	private Connection conn;
	private ResultSet rs;
	
	public AlunoEmTurmaDAO() throws Exception {
		try {	
			conn = ConnectionFactory.getConnection();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void salvar(AlunoEmTurma alunoTurma) throws Exception {
		try {
			String sql = "INSERT INTO AlunoEmTurma (Aluno_rgm, Turma_id) " + 
					"SELECT ?, id FROM Turma " + 
					"WHERE periodo = ? AND curso_id = (SELECT id FROM Curso WHERE nome = ? AND campus = ?) " +
					"AND semestre = ?";

			ps = conn.prepareStatement(sql);
			
			ps.setString(1, alunoTurma.getAluno().getRgm());
			ps.setString(2, alunoTurma.getTurma().getPeriodo());
			ps.setString(3, alunoTurma.getTurma().getCurso().getNome());
			ps.setString(4, alunoTurma.getTurma().getCurso().getCampus());
			ps.setString(5, alunoTurma.getTurma().getSemestre());

			if(ps.executeUpdate() == 0)
				throw new Exception("Aluno, curso ou turma não encontrados, verifique os dados.");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			if (e.getMessage().toLowerCase().contains("for key 'alunoemturma.primary'"))
				throw new Exception("Esse aluno já está cadastrado em um curso.");
			else if (e.getMessage().contains("Cannot add or update a child row: a foreign key constraint fails"))
				throw new Exception("Aluno não encontrado");
			throw new Exception(e.getMessage());
		}
	}
	
	public AlunoEmTurma consultar(String rgm) throws Exception{
		try {
			String sql = "SELECT Aluno_rgm, aluno.nome as nome_aluno, Turma_id, Curso_id, periodo, semestre, curso.nome as nome_curso, campus " + 
					"FROM AlunoEmTurma " + 
					"INNER JOIN Turma turma ON turma.id = AlunoEmTurma.Turma_id " + 
					"INNER JOIN Curso curso ON turma.curso_id = curso.id " + 
					"INNER JOIN Aluno aluno ON aluno.rgm = AlunoEmTurma.Aluno_rgm " + 
					"WHERE AlunoEmTurma.Aluno_rgm = ?";
			
			ps = conn.prepareStatement(sql);
			
			ps.setString(1, rgm);
			
			rs = ps.executeQuery();
			
			if(rs.next()) {
				Aluno aluno = new Aluno();
				aluno.setRgm(rs.getString("Aluno_rgm"));
				aluno.setNome(rs.getString("nome_aluno"));
				
				Curso curso = new Curso(rs.getInt("Curso_id"), rs.getString("nome_curso"), rs.getString("campus"));
				
				Turma turma = new Turma(rs.getInt("Turma_id"), curso, rs.getString("periodo"), rs.getString("semestre"));
					
				AlunoEmTurma AlunoTurma = new AlunoEmTurma(aluno, turma);
				
				return AlunoTurma;
				
			} else {
				throw new Exception("Nada encontrado.");
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public void alterar(AlunoEmTurma alunoTurma) throws Exception {
		try {
		
			excluir(alunoTurma.getAluno().getRgm());
			salvar(alunoTurma);
			
		} catch (Exception e) {
			if (e.getMessage().toLowerCase().contains("'turma_id' cannot be null"))
				throw new Exception("Turma não existe.");
			throw new Exception(e.getMessage());
		}
	}
	
	public void excluir(String rgm) throws Exception {
		try {
			String sql = "DELETE FROM AlunoEmTurma WHERE AlunoEmTurma.Aluno_rgm = ?";
			
			if (rgm.contains("        ")) {
				throw new Exception("RGM incorreto");
			}
			
			ps = conn.prepareStatement(sql);
			
			ps.setString(1, rgm);
			
			ps.executeUpdate();
			
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
}
