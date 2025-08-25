package mapper.engine;

import core.Instruction;
import dto.engine.InstructionDTO;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class InstructionDTOMapper implements Function<Instruction, InstructionDTO>
{
    @Override
    public InstructionDTO apply(Instruction instruction)
    {
        Map<InstructionDTO, Integer> derivedFromInstructionsDto = new LinkedHashMap<>();
        instruction.getDerivedInstructions().forEach((derivedInstruction, index) ->
                derivedFromInstructionsDto.put(new InstructionDTOMapper().apply(derivedInstruction), index)
        );
        return new InstructionDTO(
                instruction.getType().toString(),
                instruction.getLabel(),
                instruction.getMainVarName(),
                instruction.getCycles(),
                derivedFromInstructionsDto
        );
    }
}
